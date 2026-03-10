package com.erp.moveis.promob.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

@Service
@Slf4j
public class PromobFileWatcherService {

    private final PromobImportService importService;

    @Value("${promob.watch.enabled:false}")
    private boolean watchEnabled;

    @Value("${promob.watch.directory:C:/Promob/Export}")
    private String watchDirectory;

    @Value("${promob.watch.companyId:1}")
    private Long companyId;

    public PromobFileWatcherService(PromobImportService importService) {
        this.importService = importService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startWatching() {
        if (!watchEnabled) {
            log.info("[PROMOB_WATCHER] Desabilitado. Para ativar: promob.watch.enabled=true");
            return;
        }

        Path dir = Paths.get(watchDirectory);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
                log.info("[PROMOB_WATCHER] Diretório criado: {}", dir);
            } catch (IOException e) {
                log.error("[PROMOB_WATCHER] Não foi possível criar diretório: {}", dir, e);
                return;
            }
        }

        Thread watchThread = new Thread(() -> watchLoop(dir), "promob-file-watcher");
        watchThread.setDaemon(true);
        watchThread.start();
        log.info("[PROMOB_WATCHER] Monitorando pasta: {}", dir);
    }

    private void watchLoop(Path dir) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = dir.resolve((Path) event.context());
                        processFile(filePath);
                    }
                }

                if (!key.reset()) {
                    log.warn("[PROMOB_WATCHER] Watch key inválida, encerrando monitoramento");
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("[PROMOB_WATCHER] Monitoramento interrompido");
        } catch (Exception e) {
            log.error("[PROMOB_WATCHER] Erro no monitoramento", e);
        }
    }

    private void processFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();

        // Wait a moment for file to be completely written
        try { Thread.sleep(1000); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        try {
            byte[] content = Files.readAllBytes(filePath);

            if (fileName.endsWith(".xml")) {
                log.info("[PROMOB_WATCHER] Novo XML detectado: {}", filePath);
                var multipart = new SimpleMultipartFile(
                        filePath.getFileName().toString(),
                        "application/xml", content);
                var result = importService.importProjectXml(companyId, multipart);
                log.info("[PROMOB_WATCHER] Projeto importado: {} → Pedido #{}", fileName, result.getOrderId());

                // Move processed file
                Path processed = filePath.getParent().resolve("processados");
                Files.createDirectories(processed);
                Files.move(filePath, processed.resolve(filePath.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);

            } else if (fileName.endsWith(".csv") || fileName.endsWith(".tsv") || fileName.endsWith(".txt")) {
                log.info("[PROMOB_WATCHER] Arquivo de corte detectado (importar manualmente via API): {}", fileName);
            }
        } catch (Exception e) {
            log.error("[PROMOB_WATCHER] Erro ao processar arquivo: {}", filePath, e);
        }
    }

    /**
     * Simple MultipartFile implementation for file watcher (avoids test dependency).
     */
    private record SimpleMultipartFile(String originalFilename, String contentType,
                                        byte[] bytes) implements MultipartFile {
        @Override public String getName() { return "file"; }
        @Override public String getOriginalFilename() { return originalFilename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return bytes.length == 0; }
        @Override public long getSize() { return bytes.length; }
        @Override public byte[] getBytes() { return bytes; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }
        @Override public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), bytes);
        }
    }
}
