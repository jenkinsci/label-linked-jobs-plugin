# Label Linked Jobs Plugin
==============================
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/label-linked-jobs/master)](https://ci.jenkins.io/job/plugins/job/label-linked-jobs/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/favorite-view.svg)](https://plugins.jenkins.io/label-linked-jobs/)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/favorite-view.svg?color=blue)](https://plugins.jenkins.io/label-linked-jobs/)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/label-linked-jobs.svg)](https://github.com/jenkinsci/label-linked-jobs/graphs/contributors)


This plugin can list all the labels and related jobs.

# Get started

[jcli](https://github.com/jenkins-zh/jenkins-cli) could help you upload this plugin into your
Jenkins. The command is `jcli plugin upload`.

# API

URL: `GET http://localhost:8080/labelsdashboard/labelsData`

Response:
```
{
  "status": "ok",
  "data": [
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
  ]
}
```
