package com.erp.moveis.promob.controller;

import com.erp.moveis.promob.dto.ImportResultResponse;
import com.erp.moveis.promob.dto.PromobProjectResponse;
import com.erp.moveis.promob.service.PromobImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/promob")
@RequiredArgsConstructor
public class PromobController {

    private final PromobImportService importService;

    /**
     * Importa projeto XML do Promob.
     * Cria automaticamente: Cliente, Produtos, Pedido + Itens.
     */
    @PostMapping(value = "/import-project", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResultResponse importProject(
            @RequestParam(defaultValue = "1") Long companyId,
            @RequestPart("file") MultipartFile file) {
        return importService.importProjectXml(companyId, file);
    }

    /**
     * Importa lista de corte (CSV/TSV) do Promob.
     * Cria automaticamente: Peças de corte + Ordens de produção.
     */
    @PostMapping(value = "/import-cutlist", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResultResponse importCutlist(
            @RequestParam(defaultValue = "1") Long companyId,
            @RequestParam Long projectId,
            @RequestPart("file") MultipartFile file) {
        return importService.importCutlistCsv(companyId, projectId, file);
    }

    /**
     * Lista todos projetos Promob importados.
     */
    @GetMapping("/projects")
    public List<PromobProjectResponse> listProjects(
            @RequestParam(defaultValue = "1") Long companyId) {
        return importService.listProjects(companyId);
    }

    /**
     * Detalhe de um projeto com itens e lista de corte.
     */
    @GetMapping("/projects/{id}")
    public PromobProjectResponse getProject(@PathVariable Long id) {
        return importService.getProject(id);
    }
}
