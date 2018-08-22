package de.adesso.projectboard.core.rest;

import de.adesso.projectboard.core.base.project.persistence.AbstractProject;
import de.adesso.projectboard.core.base.project.persistence.ProjectRepository;
import de.adesso.projectboard.core.base.rest.ProjectApplication;
import de.adesso.projectboard.core.base.rest.ProjectApplicationHandler;
import de.adesso.projectboard.core.base.rest.exceptions.ProjectNotFoundException;
import de.adesso.projectboard.core.mail.ApplicationTemplateMessage;
import de.adesso.projectboard.core.mail.MailService;
import de.adesso.projectboard.core.project.persistence.JiraProject;
import org.springframework.mail.SimpleMailMessage;

import java.util.Optional;

public class JiraProjectApplicationHandler implements ProjectApplicationHandler {

    private final ProjectRepository projectRepository;

    private final MailService mailService;

    public JiraProjectApplicationHandler(ProjectRepository projectRepository, MailService mailService) {
        this.projectRepository = projectRepository;
        this.mailService = mailService;
    }

    @Override
    public void onApplicationReceived(ProjectApplication application) {
        Optional<AbstractProject> optionalProject = projectRepository.findById(application.getProjectId());

        if(optionalProject.isPresent()) {
            JiraProject jiraProject = (JiraProject) optionalProject.get();

            SimpleMailMessage message = new ApplicationTemplateMessage(jiraProject, application.getComment());


            mailService.sendMessage(message);
        } else {
            throw new ProjectNotFoundException();
        }
    }

}
