package nokia.com

import com.sap.gateway.ip.core.customdev.processor.MessageImpl
import com.sap.gateway.ip.core.customdev.util.Message
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange
import spock.lang.Shared
import spock.lang.Specification

class Tester extends Specification {

    Message msg
    Exchange exchange
    @Shared script

    def setup() {
        // Load Groovy Script
        GroovyShell shell = new GroovyShell()

        //Purchase Requisition validation  
        //script = shell.parse(new File("C:/Users/ribernar/IdeaProjects/PurchaseRequisitionValidation_to_ARBCIG_DERIVATION/src/nokia/com/PurchaseRequisitionValidation.groovy"))
        //script = shell.parse(new File("C:/Users/ribernar/IdeaProjects/PurchaseRequisitionValidation_to_ARBCIG_DERIVATION/src/nokia/com/PurchaseRequisitionValidationReply.groovy"))

        //Invoice Validation
        //script = shell.parse(new File("C:/Users/ribernar/IdeaProjects/PurchaseRequisitionValidation_to_ARBCIG_DERIVATION/src/nokia/com/InvoiceValidation.groovy"))

        //Contract Validation
        script = shell.parse(new File("C:/Users/ribernar/IdeaProjects/PurchaseRequisitionValidation_to_ARBCIG_DERIVATION/src/nokia/com/ContractValidationReply.groovy"))

        CamelContext context = new DefaultCamelContext()
        exchange = new DefaultExchange(context)
        msg = new MessageImpl(exchange)
    }

    def 'mapping'() {
        given:
        //--------------------------------------------------------------
        // Initialize message with body, header and property
        def body = new File("C:/Users/ribernar/IdeaProjects/PurchaseRequisitionValidation_to_ARBCIG_DERIVATION/etc/inputErrorReply.txt")
        msg.setProperty('DocType', 'Z001')
        //--------------------------------------------------------------

        exchange.getIn().setBody(body.text)
        msg.setBody(exchange.getIn().getBody())

        // when:
        // Execute script
        script.processData(msg)

        // then:
        //msg.getBody() == new File("C:/Users/ribernar/IdeaProjects/mapEndpoint_to_Sharepoint/etc/output.txt").text

        File output = new File("C:/Users/ribernar/IdeaProjects/PurchaseRequisitionValidation_to_ARBCIG_DERIVATION/etc/output.txt")
        output.write(msg.getBody().toString())
    }
}