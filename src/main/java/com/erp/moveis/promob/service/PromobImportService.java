package com.erp.moveis.promob.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.manufacturing.service.ManufacturingService;
import com.erp.moveis.model.*;
import com.erp.moveis.promob.dto.*;
import com.erp.moveis.promob.entity.*;
import com.erp.moveis.promob.repository.*;
import com.erp.moveis.repository.ClientRepository;
import com.erp.moveis.repository.ProductRepository;
import com.erp.moveis.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromobImportService {

    private final PromobProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;
    private final ManufacturingService manufacturingService;

    // ── Import XML project ──────────────────────────────────────

    @Transactional
    public ImportResultResponse importProjectXml(Long companyId, MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xml")) {
            throw new BusinessException("Arquivo deve ser XML (.xml)");
        }

        if (projectRepository.existsByCompanyIdAndFileName(companyId, fileName)) {
            throw new BusinessException("Projeto já importado: " + fileName);
        }

        try (InputStream is = file.getInputStream()) {
            return parseAndImportXml(companyId, fileName, is);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PROMOB_IMPORT] Error importing XML: {}", fileName, e);
            throw new BusinessException("Erro ao processar arquivo XML: " + e.getMessage());
        }
    }

    private ImportResultResponse parseAndImportXml(Long companyId, String fileName, InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Security: disable external entities to prevent XXE
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        doc.getDocumentElement().normalize();

        Element root = doc.getDocumentElement();

        // Extract client name
        String clientName = getTextContent(root, "client");
        if (clientName == null) clientName = getTextContent(root, "cliente");
        if (clientName == null) clientName = getTextContent(root, "Client");

        String environment = getTextContent(root, "environment");
        if (environment == null) environment = getTextContent(root, "ambiente");

        String designer = getTextContent(root, "designer");
        if (designer == null) designer = getTextContent(root, "projetista");

        // Find or create client
        Client client = findOrCreateClient(clientName);

        // Parse items
        List<PromobProjectItem> items = new ArrayList<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        NodeList itemNodes = root.getElementsByTagName("item");
        if (itemNodes.getLength() == 0) itemNodes = root.getElementsByTagName("Item");
        if (itemNodes.getLength() == 0) itemNodes = root.getElementsByTagName("produto");

        for (int i = 0; i < itemNodes.getLength(); i++) {
            Element el = (Element) itemNodes.item(i);

            String name = getTextContent(el, "name");
            if (name == null) name = getTextContent(el, "nome");
            if (name == null) name = getTextContent(el, "Name");
            if (name == null) name = "Item " + (i + 1);

            String priceStr = getTextContent(el, "price");
            if (priceStr == null) priceStr = getTextContent(el, "preco");
            if (priceStr == null) priceStr = getTextContent(el, "valor");
            BigDecimal price = parseDecimal(priceStr);

            String qtyStr = getTextContent(el, "quantity");
            if (qtyStr == null) qtyStr = getTextContent(el, "quantidade");
            int qty = qtyStr != null ? Integer.parseInt(qtyStr.trim()) : 1;

            String desc = getTextContent(el, "description");
            if (desc == null) desc = getTextContent(el, "descricao");

            BigDecimal width = parseDecimal(getTextContent(el, "width"));
            if (width == null) width = parseDecimal(getTextContent(el, "largura"));

            BigDecimal height = parseDecimal(getTextContent(el, "height"));
            if (height == null) height = parseDecimal(getTextContent(el, "altura"));

            BigDecimal depth = parseDecimal(getTextContent(el, "depth"));
            if (depth == null) depth = parseDecimal(getTextContent(el, "profundidade"));

            // Find or create product
            Product product = findOrCreateProduct(name, price);

            PromobProjectItem item = PromobProjectItem.builder()
                    .name(name)
                    .description(desc)
                    .productId(product.getId())
                    .quantity(qty)
                    .unitPrice(price)
                    .totalPrice(price != null ? price.multiply(BigDecimal.valueOf(qty)) : null)
                    .width(width)
                    .height(height)
                    .depth(depth)
                    .build();
            items.add(item);

            if (price != null) {
                totalValue = totalValue.add(price.multiply(BigDecimal.valueOf(qty)));
            }
        }

        // Create Promob project
        PromobProject project = PromobProject.builder()
                .companyId(companyId)
                .fileName(fileName)
                .clientName(clientName)
                .clientId(client != null ? client.getId() : null)
                .environment(environment)
                .designer(designer)
                .totalValue(totalValue)
                .status("IMPORTED")
                .build();

        // Link items
        for (PromobProjectItem item : items) {
            item.setProject(project);
        }
        project.setItems(items);

        projectRepository.save(project);

        // Create Order in ERP
        Order order = createOrderFromProject(companyId, project, client, items);
        project.setOrderId(order.getId());
        project.setStatus("ORDER_CREATED");
        projectRepository.save(project);

        log.info("[PROMOB_IMPORT] Project '{}' imported: {} items, order #{}, total R$ {}",
                fileName, items.size(), order.getId(), totalValue);

        return ImportResultResponse.builder()
                .projectId(project.getId())
                .fileName(fileName)
                .clientName(clientName)
                .clientId(client != null ? client.getId() : null)
                .orderId(order.getId())
                .itemsImported(items.size())
                .cutlistPartsImported(0)
                .productionOrdersCreated(0)
                .totalValue(totalValue)
                .status("ORDER_CREATED")
                .message("Projeto importado com sucesso! Pedido #" + order.getId() + " criado.")
                .build();
    }

    // ── Import CSV cutlist ──────────────────────────────────────

    @Transactional
    public ImportResultResponse importCutlistCsv(Long companyId, Long projectId, MultipartFile file) {
        PromobProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("Projeto não encontrado: " + projectId));

        String fileName = file.getOriginalFilename();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return parseAndImportCutlist(companyId, project, fileName, reader);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PROMOB_CUTLIST] Error importing cutlist: {}", fileName, e);
            throw new BusinessException("Erro ao processar lista de corte: " + e.getMessage());
        }
    }

    private ImportResultResponse parseAndImportCutlist(Long companyId, PromobProject project,
                                                        String fileName, BufferedReader reader) throws Exception {
        List<PromobCutlistPart> parts = new ArrayList<>();
        String line;
        boolean headerSkipped = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("---")) continue;

            // Detect and skip header
            if (!headerSkipped) {
                String lower = line.toLowerCase();
                if (lower.contains("peça") || lower.contains("peca") || lower.contains("part")
                        || lower.contains("material") || lower.contains("nome")) {
                    headerSkipped = true;
                    continue;
                }
                headerSkipped = true;
            }

            // Parse CSV/TSV line
            String[] cols = line.split("[;,\t]+");
            if (cols.length < 2) continue;

            String partName = cols[0].trim();
            String material = cols.length > 1 ? cols[1].trim() : null;
            BigDecimal height = cols.length > 2 ? parseDecimal(cols[2].trim()) : null;
            BigDecimal width = cols.length > 3 ? parseDecimal(cols[3].trim()) : null;
            BigDecimal thickness = null;
            int qty = 1;

            // Try to detect thickness and quantity from extra columns
            if (cols.length > 4) thickness = parseDecimal(cols[4].trim());
            if (cols.length > 5) {
                try { qty = Integer.parseInt(cols[5].trim()); } catch (NumberFormatException ignored) {}
            }

            // Extract thickness from material string (e.g. "MDF 18mm")
            if (thickness == null && material != null) {
                thickness = extractThicknessFromMaterial(material);
            }

            PromobCutlistPart part = PromobCutlistPart.builder()
                    .project(project)
                    .partName(partName)
                    .material(material)
                    .height(height)
                    .width(width)
                    .thickness(thickness)
                    .quantity(qty)
                    .build();
            parts.add(part);
        }

        project.getCutlistParts().addAll(parts);

        // Create production orders for each unique product in project
        int productionOrdersCreated = 0;
        for (PromobProjectItem item : project.getItems()) {
            if (item.getProductId() != null) {
                try {
                    var po = manufacturingService.createProductionOrder(
                            companyId, item.getProductId(),
                            BigDecimal.valueOf(item.getQuantity()));
                    productionOrdersCreated++;
                    log.info("[PROMOB_CUTLIST] Production order created for product #{}", item.getProductId());
                } catch (Exception e) {
                    log.warn("[PROMOB_CUTLIST] Could not create production order for product #{}: {}",
                            item.getProductId(), e.getMessage());
                }
            }
        }

        project.setStatus("PRODUCTION_CREATED");
        projectRepository.save(project);

        log.info("[PROMOB_CUTLIST] Cutlist imported for project #{}: {} parts, {} production orders",
                project.getId(), parts.size(), productionOrdersCreated);

        return ImportResultResponse.builder()
                .projectId(project.getId())
                .fileName(fileName)
                .clientName(project.getClientName())
                .clientId(project.getClientId())
                .orderId(project.getOrderId())
                .itemsImported(0)
                .cutlistPartsImported(parts.size())
                .productionOrdersCreated(productionOrdersCreated)
                .totalValue(project.getTotalValue())
                .status("PRODUCTION_CREATED")
                .message("Lista de corte importada! " + parts.size() + " peças, "
                        + productionOrdersCreated + " ordens de produção criadas.")
                .build();
    }

    // ── List projects ───────────────────────────────────────────

    public List<PromobProjectResponse> listProjects(Long companyId) {
        return projectRepository.findByCompanyIdOrderByImportedAtDesc(companyId)
                .stream().map(this::toResponse).toList();
    }

    public PromobProjectResponse getProject(Long id) {
        PromobProject p = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Projeto não encontrado: " + id));
        return toDetailResponse(p);
    }

    // ── Helpers ─────────────────────────────────────────────────

    private Client findOrCreateClient(String clientName) {
        if (clientName == null || clientName.isBlank()) return null;

        return clientRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(clientName.trim()))
                .findFirst()
                .orElseGet(() -> {
                    Client c = new Client(clientName.trim(), null, null, null, "Importado Promob");
                    return clientRepository.save(c);
                });
    }

    private Product findOrCreateProduct(String name, BigDecimal price) {
        return productRepository.findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElseGet(() -> {
                    Product p = new Product(name.trim(), null, null,
                            price != null ? price.doubleValue() : 0.0);
                    return productRepository.save(p);
                });
    }

    private Order createOrderFromProject(Long companyId, PromobProject project,
                                          Client client, List<PromobProjectItem> items) {
        Order order = new Order();
        order.setCompanyId(companyId);
        order.setClient(client);
        order.setStatus("PENDING");

        List<OrderItem> orderItems = new ArrayList<>();
        for (PromobProjectItem pi : items) {
            OrderItem oi = OrderItem.builder()
                    .productId(pi.getProductId())
                    .quantity(BigDecimal.valueOf(pi.getQuantity()))
                    .unitPrice(pi.getUnitPrice() != null ? pi.getUnitPrice() : BigDecimal.ZERO)
                    .build();
            oi.calculateSubtotal();
            orderItems.add(oi);
        }

        order.setItems(orderItems);
        for (OrderItem oi : orderItems) {
            oi.setOrder(order);
        }
        order.recalculateTotal();

        return orderService.save(order);
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            String text = nodes.item(0).getTextContent();
            return (text != null && !text.isBlank()) ? text.trim() : null;
        }
        return null;
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            String cleaned = value.replaceAll("[^0-9.,\\-]", "").replace(",", ".");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal extractThicknessFromMaterial(String material) {
        // e.g. "MDF 18mm" → 18, "MDP 15" → 15
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*mm").matcher(material);
        if (m.find()) {
            return new BigDecimal(m.group(1));
        }
        return null;
    }

    private PromobProjectResponse toResponse(PromobProject p) {
        return PromobProjectResponse.builder()
                .id(p.getId())
                .companyId(p.getCompanyId())
                .fileName(p.getFileName())
                .clientName(p.getClientName())
                .clientId(p.getClientId())
                .orderId(p.getOrderId())
                .environment(p.getEnvironment())
                .designer(p.getDesigner())
                .totalValue(p.getTotalValue())
                .status(p.getStatus())
                .notes(p.getNotes())
                .importedAt(p.getImportedAt())
                .itemCount(p.getItems() != null ? p.getItems().size() : 0)
                .cutlistCount(p.getCutlistParts() != null ? p.getCutlistParts().size() : 0)
                .build();
    }

    private PromobProjectResponse toDetailResponse(PromobProject p) {
        PromobProjectResponse resp = toResponse(p);
        resp.setItems(p.getItems().stream().map(i -> PromobItemResponse.builder()
                .id(i.getId())
                .name(i.getName())
                .description(i.getDescription())
                .productId(i.getProductId())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .totalPrice(i.getTotalPrice())
                .width(i.getWidth())
                .height(i.getHeight())
                .depth(i.getDepth())
                .build()).toList());
        resp.setCutlist(p.getCutlistParts().stream().map(c -> PromobCutlistResponse.builder()
                .id(c.getId())
                .partName(c.getPartName())
                .material(c.getMaterial())
                .thickness(c.getThickness())
                .width(c.getWidth())
                .height(c.getHeight())
                .quantity(c.getQuantity())
                .edgeTop(c.getEdgeTop())
                .edgeBottom(c.getEdgeBottom())
                .edgeLeft(c.getEdgeLeft())
                .edgeRight(c.getEdgeRight())
                .notes(c.getNotes())
                .productionOrderId(c.getProductionOrderId())
                .build()).toList());
        return resp;
    }
}
