# Label Linked Jobs Plugin
==============================
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/label-linked-jobs/master)](https://ci.jenkins.io/job/plugins/job/label-linked-jobs/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/favorite-view.svg)](https://plugins.jenkins.io/label-linked-jobs/)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/favorite-view.svg?color=blue)](https://plugins.jenkins.io/label-linked-jobs/)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/label-linked-jobs.svg)](https://github.com/jenkinsci/label-linked-jobs/graphs/contributors)


This plugin can list all the labels and related jobs.

# Get started

[jcli](https://github.com/jenkins-zh/jenkins-cli) could help you upload this plugin into your Jenkins. The command is `jcli plugin upload`.

# API

## Endpoint

The plugin exposes a REST endpoint that returns information about Jenkins labels, nodes and their related jobs.
The response format is selected by the URL suffix: `json` or `xml`.

```http
GET http://localhost:8080/labelsdashboard/api/json
```

## Example request

Run this command against a Jenkins instance where the Label Linked Jobs plugin is installed:

```bash
curl -u USERNAME:API_TOKEN \
  "http://localhost:8080/labelsdashboard/api/json"
```

Replace `USERNAME` and `API_TOKEN` with your Jenkins username and API token. The Jenkins instance must be accessible at the specified URL.

## Response

The endpoint returns a JSON object with the `labels` and `nodes` arrays.

Example response:

```json
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
  "nodes": [
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

The values shown are illustrative. Actual values depend on the Jenkins configuration.