package cinergi.annotator

import grails.test.spock.IntegrationSpec

/**
 * Created by bozyurt on 3/13/15.
 */
class AnnotationServiceSpec extends IntegrationSpec {
     def annotationService

     void "test generateIsoXml"() {
         String primaryKey = 'OT.092012.26913.2'
         String outFile
         when: "generateIsoXml is called"
         outFile = annotationService.generateIsoXml(primaryKey)
         then: "should work"
         outFile
         new File(outFile).isFile()
     }
}
