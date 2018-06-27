Developing the Nokia v2 SVNFM adapter
=====================================


Quality gate
------------

- 99.9 % unit test coverage

- near 0 sonar issues (with the exceptions of FIXME issues)

Useful links
------------

- Jenkins build bot: https://jenkins.onap.org/view/vfc/job/vfc-nfvo-driver-svnfm-nokiav2-master-drv-vnfm-nokiav2-verify-java/

- Jenkins sonar analyitcs: https://jenkins.onap.org/view/vfc/job/vfc-nfvo-driver-vnfm-svnfm-nokiav2-sonar/

- Jenkins CLM: https://jenkins.onap.org/view/vfc/job/vfc-nfvo-driver-svnfm-nokiav2-maven-clm-master/

- Jenkins daily release https://jenkins.onap.org/view/vfc/job/vfc-nfvo-driver-svnfm-nokiav2-master-drv-vnfm-nokiav2-release-version-java-daily/

- Jenkins snapshot docker release: https://jenkins.onap.org/view/vfc/job/vfc-nfvo-driver-svnfm-nokiav2-docker-vnfm-nokiav2-master-merge-docker/

- Jenkins daily docker release: https://jenkins.onap.org/view/vfc/job/vfc-nfvo-driver-svnfm-nokiav2-docker-vnfm-nokiav2-master-release-version-docker-daily-no-sonar/

- Sonar: https://sonar.onap.org/dashboard?id=org.onap.vfc.nfvo.driver.vnfm.svnfm.nokiav2%3Avfc-nfvo-driver-vnfm-svnfm-nokiav2

- Nexus IQ reports: https://nexus-iq.wl.linuxfoundation.org/assets/index.html#/management/view/application/vfc-nfvo-driver-svnfm-nokiav2

Backlog
-------

- Support micro service auto configuration

- Support logging configuration

- Support for operation traces in logging https://wiki.onap.org/pages/viewpage.action?pageId=20087036

- Add CSIT using SVNFM simulator

Recently solved issues
----------------------

- add driver to OOM based deployments

- Solve remaining severe & moderate license issues




