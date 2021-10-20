## Change Log

#### Future

-   localization
-   deal with multi-configuration/matrix projects
-   [Jenkins-27907](https://issues.jenkins-ci.org/browse/JENKINS-27907),
    UI rework

#### 5.1.2 (released 2016-11-19)

-   [Jenkins-38342](https://issues.jenkins-ci.org/browse/JENKINS-38342),
    show all available labels provided by clouds in Labels Dashboard

#### 5.0.1 (released 2016-05-08)

-   [Jenkins-32445](https://issues.jenkins-ci.org/browse/JENKINS-32445),
    make plugin compatible with Jenkins Clouds' definition

#### 4.0.3 (released 2015-12-26)

-   fix for
    [Jenkins-32049](https://issues.jenkins-ci.org/browse/JENKINS-32049),
    Labels Dashboard is not showing any Labels and any Nodes

#### 4.0.2 (released 2015-04-28)

-   following the 4.0.1 release, ignore the $TOKEN\_NAME syntax in
    labels in NodeLabel Parameter plugin settings and Parameterized
    Trigger plugin settings, in addition to the ${TOKEN\_NAME} syntax

#### 4.0.1 (released 2015-04-23)

-   [Jenkins-27588](https://issues.jenkins-ci.org/browse/JENKINS-27588),
    make plugin compatible with [NodeLabel Parameter
    Plugin](https://wiki.jenkins-ci.org/display/JENKINS/NodeLabel+Parameter+Plugin)
    and [Parameterized Trigger
    Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Parameterized+Trigger+Plugin),
    to show "triggered jobs", based on their label configuration, on the
    various pages of Label Linked Jobs Plugin

#### 3.0.3 (released 2014-10-20)

-   [Jenkins-25163](https://issues.jenkins-ci.org/browse/JENKINS-25163),
    add "Jobs with no labels" section in Labels Dashboard
-   [Jenkins-25188](https://issues.jenkins-ci.org/browse/JENKINS-25188),
    orphaned jobs do not show jobs without label when all nodes set to
    Label restrictions

#### 2.0.4 (released 2014-09-24)

-   [Jenkins-20035](https://issues.jenkins-ci.org/browse/JENKINS-20035),
    new "Linked Jobs" page per node (including master) to list all jobs
    linked, per label configuration, to this specific node. Can also
    list jobs that can run exclusively on the given node because of
    label configuration
-   [Jenkins-24615](https://issues.jenkins-ci.org/browse/JENKINS-24615),
    new "Labels Dashboard" to have a global view of all labels defined
    and used in jobs & nodes, list orphaned jobs, list single-node jobs
-   [Jenkins-24641](https://issues.jenkins-ci.org/browse/JENKINS-24641),
    see what could be reused (ideas and/or code) from [Daniel's similar
    plugin](https://github.com/daniel-beck/better-labels-plugin)
-   requires Jenkins 1.554 (subsequent LTS) to use new icon

#### 1.0.1 (release 2014-09-04)

-   initial release to address
    [Jenkins-23333](https://issues.jenkins-ci.org/browse/JENKINS-23333)
-   thanks to Daniel Beck for his feedback and support :-)
-   requires Jenkins 1.532