# Label Linked Jobs Plugin

This plugin can list all the labels and related jobs.

# Get started

[jcli](https://github.com/jenkins-zh/jenkins-cli) could help you upload this plugin into your
Jenkins. The command is `jcli plugin upload`.

# API

## labels

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

## nodes

URL: `GET http://localhost:8080/labelsdashboard/nodesData`

Response:
```
{
    "status": "ok",
    "data": [
        {
            "hasMoreThanOneJob": false,
            "jobs": [],
            "jobsCount": 0,
            "jobsWithLabelDefaultValue": [],
            "jobsWithLabelDefaultValueCount": 0,
            "labelURL": "label/master/",
            "name": "Jenkins",
            "nodeURL": "computer/(master)/",
            "triggeredJobs": [],
            "triggeredJobsCount": 0
        },
        {
            "hasMoreThanOneJob": false,
            "jobs": [],
            "jobsCount": 0,
            "jobsWithLabelDefaultValue": [],
            "jobsWithLabelDefaultValueCount": 0,
            "labelURL": "label/macos1/",
            "name": "macos1",
            "nodeURL": "computer/macos1/",
            "triggeredJobs": [],
            "triggeredJobsCount": 0
        }
    ]
}
```
