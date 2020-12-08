# Label Linked Jobs Plugin

This plugin can list all the labels and related jobs.

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
