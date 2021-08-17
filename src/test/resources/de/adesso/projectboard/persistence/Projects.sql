--Create Projects, some with other statuses, LoBs and no LoB (null)
INSERT INTO PROJECT (ID, STATUS, ISSUE_TYPE, TITLE, JOB, SKILLS, DESCRIPTION, LOB, CUSTOMER, LOCATION, OPERATION_START, OPERATION_END, EFFORT, CREATED, UPDATED, FREELANCER, ELONGATION, OTHER, DAILY_RATE, TRAVEL_COSTS_COMPENSATED) VALUES
('STF-1', 'eskaliert', 'Issuetype', 'Special Title', 'Job', 'Skills', 'Java', 'LOB Test', 'Customer', 'Location', 'OperationStart', 'OperationEnd', 'Effort', '2018-02-1 13:37:00', '2018-02-2 13:37:00', 'Freelancer', 'Elongation', 'Other', 'Rate 1', 'Compensated'),

('STF-2', 'Abgeschlossen', 'Issuetype', 'Special Title', 'Job', 'Skills', 'Description', 'LOB Test', 'Customer', 'Location', 'OperationStart', 'OperationEnd', 'Effort', '2018-02-1 13:37:00', '2018-02-2 13:37:00', 'Freelancer', 'Elongation', 'Other', 'Rate 2', 'Compensated'),

('STF-3', 'eskaliert', 'Issuetype', 'Title', 'Job', 'Skills', 'Spring', 'LOB Prod', 'Customer', 'Location', 'OperationStart', 'OperationEnd', 'Effort', '2018-02-1 13:37:00', '2018-02-2 13:37:00', 'Freelancer', 'Elongation', 'Other', 'Rate 3', 'Compensated'),

('STF-4', 'offen', 'Issuetype', 'Title', 'Special Job', 'Skills', 'Description', NULL, 'Customer', 'Location', 'OperationStart', 'OperationEnd', 'Effort', '2018-02-1 13:37:00', '2018-02-2 13:37:00', 'Freelancer', 'Elongation', 'Other', 'Rate 4', 'Compensated'),

('STF-5', 'eskaliert', 'Issuetype', 'Title', 'Job', 'Special Skills', 'Description', NULL, 'Customer', 'Location', 'OperationStart', 'OperationEnd', 'Effort', '2018-02-1 13:37:00', '2018-02-2 13:37:00', 'Freelancer', 'Elongation', 'Other', 'Rate 5', 'Compensated'),

('STF-6', 'Abgeschlossen', 'Issuetype', 'Title', 'Job', 'Skills', 'Description', NULL, 'Customer', 'Location', 'OperationStart', 'OperationEnd', 'Effort', '2018-02-1 13:37:00', '2018-02-2 13:37:00', 'Freelancer', 'Elongation', 'Other', 'Rate 1', 'Compensated'),

('STF-7', 'open', 'Issuetype', 'Title', 'Job', 'Skills', 'Description', NULL, 'Customer', 'Location', 'OperationStart', 'OperationEnd', 'Effort', '2018-02-1 13:37:00', '2018-02-2 13:37:00', 'Freelancer', 'Elongation', 'Other', 'Rate 7', 'Compensated'),

('STF-8', 'Offen', 'Issuetype', 'Title', 'Job', 'Skills', 'Extraordinary Description', 'LOB Test', 'Customer', 'Location', 'OperationStart', 'OperationEnd', 'Effort', '2018-02-1 13:37:00', '2018-02-2 13:37:00', 'Freelancer', 'Elongation', 'Other', 'Rate 8', 'Compensated'),

('STF-9', 'Open', 'Issuetype', 'Title', 'Job', 'Skills', 'Description', 'LOB Prod', 'Mockito', 'Location', 'OperationStart', 'OperationEnd', 'Effort', '2018-02-1 13:37:00', '2018-02-2 13:37:00', 'Freelancer', 'Elongation', 'Other', 'Rate 9', 'Compensated'),

('STF-10', 'OPEN', 'Issuetype', 'Title', 'Job', 'Skills', 'Description', 'LOB Prod', 'Customer', 'Location', 'OperationStart', 'OperationEnd', 'Effort', '2019-02-1 13:37:00', '2019-02-2 13:37:00', 'Freelancer', 'Elongation', 'Other', 'Rate 9', 'Compensated');

--Create some Labels for STF-1:
INSERT INTO PROJECT_LABELS (PROJECT_ID, LABEL)
VALUES ('STF-1', 'Label 1'), ('STF-1', 'Label 2'), ('STF-1', 'Label 3');
