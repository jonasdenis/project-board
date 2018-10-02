import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '../_services/alert.service';
import { Project, ProjectService } from '../_services/project.service';

@Component({
  selector: 'app-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.scss']
})
export class ProjectComponent implements OnInit {
  project: Project;

  labelsInput = '';
  form: FormGroup;
  submitted = false;
  edit = false;
  navigateOnSubmit = false;

  constructor(private projectService: ProjectService,
              private alertService: AlertService,
              private formBuilder: FormBuilder,
              private route: ActivatedRoute,
              private router: Router) { }

  ngOnInit() {
    this.route.data.subscribe(data => {
      this.resetProject();
      if (data.project) {
        this.project = data.project;
        this.edit = true;
      }
      this.form = this.formBuilder.group({
        title: [this.project.title, Validators.required],
        status: [this.project.status, Validators.required],
        description: [this.project.description, Validators.required],
        lob: [this.project.lob, Validators.required],
        issuetype: [this.project.issuetype, Validators.required]
      });

      if (this.project.labels.length > 0) {
        for (let i = 0; i < this.project.labels.length - 1; i++) {
          this.labelsInput += this.project.labels[i] + ' ';
        }
        this.labelsInput += this.project.labels[this.project.labels.length - 1];
      }
    });
  }

  onSubmit() {
    this.submitted = true;

    if (this.form.invalid) {
      return;
    }
    this.project.title = this.f.title.value;
    this.project.issuetype = this.f.issuetype.value;
    this.project.status = this.f.status.value;
    this.project.lob = this.f.lob.value;
    this.project.description = this.f.description.value;
    this.project.labels = this.labelsInput.split(' ');

    if (this.edit) {
      this.projectService.updateProject(this.project).subscribe(() => {
        this.alertService.success('Änderungen wurden gespeichert.', true);
        this.router.navigate(['/overview']);
      });
    } else {
      this.projectService.createProject(this.project).subscribe(() => {
        this.alertService.success('Projekt erfolgreich erstellt.', !this.navigateOnSubmit);
        if (!this.navigateOnSubmit) {
          this.router.navigate(['/overview']);
        } else {
          this.submitted = false;
          this.resetProject();
          document.body.scrollTop = 0;
          document.documentElement.scrollTop = 0;
        }
      });
    }
  }

  get f() {
    return this.form.controls;
  }

  resetProject() {
    this.project = {
      customer: '',
      description: '',
      effort: '',
      elongation: '',
      freelancer: '',
      id: '',
      issuetype: '',
      job: '',
      lob: '',
      labels: [],
      location: '',
      operationEnd: '',
      operationStart: '',
      other: '',
      skills: '',
      status: '',
      title: '',
      created: null,
      updated: null
    };
    this.form = this.formBuilder.group({
      title: ['', Validators.required],
      status: ['', Validators.required],
      description: ['', Validators.required],
      lob: ['', Validators.required],
      issuetype: ['', Validators.required]
    });
  }
}
