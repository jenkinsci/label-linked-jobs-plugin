# Label Linked Jobs Plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/label-linked-jobs/master)](https://ci.jenkins.io/job/plugins/job/label-linked-jobs/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/favorite-view.svg)](https://plugins.jenkins.io/label-linked-jobs/)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/favorite-view.svg?color=blue)](https://plugins.jenkins.io/label-linked-jobs/)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/label-linked-jobs.svg)](https://github.com/jenkinsci/label-linked-jobs/graphs/contributors)

<a href="https://jenkins.io">
    <img width="400" src="https://www.jenkins.io/images/jenkins-logo-title-dark.svg" alt="Jenkins logo"> 
</a>

## What is it

This plugin can list all the labels and related jobs.

## Table of Contents

- [Get started](#get-started)
- [API](#api)
  - [Endpoint](#endpoint)
  - [Example request](#example-request)
  - [Response](#response)
- [Contributing](#contributing)
- [License](#license)

## Get started

[jcli](https://github.com/jenkins-zh/jenkins-cli) could help you upload this plugin into your Jenkins. The command is `jcli plugin upload`.

## API

### Endpoint

The plugin exposes a REST endpoint that returns information about Jenkins labels and their related jobs.

```http
GET http://localhost:8080/labelsdashboard/labelsData
```

### Example request

Run this command against a Jenkins instance where the Label Linked Jobs plugin is installed:

```bash
curl -u USERNAME:API_TOKEN \
  "http://localhost:8080/labelsdashboard/labelsData"
```

Replace `USERNAME` and `API_TOKEN` with your Jenkins username and API token. The Jenkins instance must be accessible at the specified URL.

### Response

The endpoint returns a JSON array containing information about Jenkins labels.

Example response:

```json
[
  {
    "label": "java",
    "description": "",
    "cloudsCount": 0,
    "nodesCount": 1,
    "jobs": [],
    "jobsCount": 0,
    "hasMoreThanOneJob": false,
    "jobsWithLabelDefaultValue": [],
    "jobsWithLabelDefaultValueCount": 0,
    "triggeredJobs": [],
    "triggeredJobsCount": 0,
    "pluginActiveForLabel": false,
    "labelURL": "label/java/"
  }
]
```

The values shown are illustrative. Actual values depend on the Jenkins configuration.

## Contributing

Contributions are what make the open source community such an amazing place to be learn, 
inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## License

Distributed under the MIT License. See [LICENSE](LICENSE) for more information.
