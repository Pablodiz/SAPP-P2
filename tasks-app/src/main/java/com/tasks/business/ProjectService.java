package com.tasks.business;

import com.tasks.business.entities.Project;
import com.tasks.business.entities.User;
import com.tasks.business.exceptions.DuplicatedResourceException;
import com.tasks.business.exceptions.InstanceNotFoundException;
import com.tasks.business.exceptions.PermissionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tasks.business.repository.ProjectsRepository;
import com.tasks.business.repository.UsersRepository;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private UsersRepository userRepository;
    
    @Transactional
    public Project create(String name, String description, String username) 
            throws DuplicatedResourceException, UsernameNotFoundException {
        Optional<User> user = userRepository.findById(username);
        if(!user.isPresent()) {
            throw new UsernameNotFoundException(MessageFormat.format("User {0} does not exist", username));
        }
        if(projectsRepository.findByName(name) != null) {
            throw new DuplicatedResourceException("Porject", name, 
                MessageFormat.format("Project ''{0}'' already exists", name));
        }
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setTimestamp(System.currentTimeMillis());
        project.setTasksCount(0);
        project.setAdmin(user.get());
        return projectsRepository.save(project);
    }

    @Transactional
    public Project update(String userName, Long projectId, String name, String description) 
            throws DuplicatedResourceException, InstanceNotFoundException {
        Optional<Project> project = projectsRepository.findById(projectId);
        if(!project.isPresent()) {
            throw new InstanceNotFoundException(projectId, "Project", 
                    MessageFormat.format("Project {0} does not exist", projectId));
        }
        Project anotherProject = projectsRepository.findByName(name);
        if(anotherProject != null && !Objects.equals(anotherProject.getProjectId(), projectId)) {
            throw new DuplicatedResourceException("Project", name, 
                MessageFormat.format("Project ''{0}'' already exists", name));
        }
        if(!project.get().getAdmin().getUsername().equals(userName)) {
            throw new PermissionException();
        }
        project.get().setName(name);
        project.get().setDescription(description);
        project.get().setTimestamp(System.currentTimeMillis());
        return projectsRepository.save(project.get());
    }
    
    @Transactional()
    public void removeById(String userName , Long id) throws InstanceNotFoundException {
        Optional<Project> project = projectsRepository.findById(id);
        if(!project.isPresent()) {
            throw new InstanceNotFoundException(id, "Project" , MessageFormat.format("Project {0} does not exist", id));
        }
        if(!project.get().getAdmin().getUsername().equals(userName)) {
            throw new PermissionException();
        }
        projectsRepository.delete(project.get());
    }
    
    @Transactional(readOnly = true)
    public Project findById(Long id) throws InstanceNotFoundException {
        Optional<Project> project = projectsRepository.findById(id);
        if(!project.isPresent()) {
            throw new InstanceNotFoundException(id, "Task" , MessageFormat.format("Project {0} does not exist", id));
        }
        return project.get();
    }
    
    @Transactional(readOnly = true)
    public Iterable<Project> findAll() {
        return projectsRepository.findAll();
    }
    
}
