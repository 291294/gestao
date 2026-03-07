package com.erp.moveis.service;

import com.erp.moveis.model.Project;
import com.erp.moveis.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository repository;

    public List<Project> list() {
        return repository.findAll();
    }

    public Optional<Project> findById(Long id) {
        return repository.findById(id);
    }

    public List<Project> findByClientId(Long clientId) {
        return repository.findByClientId(clientId);
    }

    public Project save(Project project) {
        return repository.save(project);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Project update(Long id, Project projectDetails) {
        Optional<Project> project = repository.findById(id);
        if (project.isPresent()) {
            Project existingProject = project.get();
            if (projectDetails.getName() != null) {
                existingProject.setName(projectDetails.getName());
            }
            if (projectDetails.getDescription() != null) {
                existingProject.setDescription(projectDetails.getDescription());
            }
            if (projectDetails.getBudget() != null) {
                existingProject.setBudget(projectDetails.getBudget());
            }
            return repository.save(existingProject);
        }
        return null;
    }
}