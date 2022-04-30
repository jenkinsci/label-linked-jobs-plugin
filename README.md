# Label Linked Jobs Plugin

This plugin can list all the labels and related jobs.

# Get started

[jcli](https://github.com/jenkins-zh/jenkins-cli) could help you upload this plugin into your
Jenkins. The command is `jcli plugin upload`.

# API

URL: `GET http://localhost:8080/labelsdashboard/api/json`

Response:
```
{
  "labels": [
    {
      "cloudsCount": 0,
      "description": "",
      "hasMoreThanOneJob": false,
      "jobs": [],
      "jobsCount": 0,
      "jobsWithLabelDefaultValue": [],
      "jobsWithLabelDefaultValueCount": 0,
      "label": "java",
      "labelURL": "label/java/",
      "nodesCount": 1,
      "pluginActiveForLabel": false,
      "triggeredJobs": [],
      "triggeredJobsCount": 0
    }
  ],
  "nodes" : [
    {
      "hasMoreThanOneJob": false,
      "jobs": [],
      "jobsCount": 0,
      "jobsWithLabelDefaultValue": [],
      "jobsWithLabelDefaultValueCount": 0,
      "triggeredJobs": [],
      "triggeredJobsCount": 0,
      "labelURL": "label/master/",
      "name": "Jenkins",
      "nodeURL": "computer/(master)/"
    }
  ]
}
```
