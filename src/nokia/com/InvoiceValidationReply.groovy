package nokia.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

def Message processData(Message message) {
    // Access message body and properties
    Reader reader = message.getBody(Reader)
    //Map properties = message.getProperties()

    // Define XML parser and builder
    def AribaReply = new XmlSlurper().parse(reader)
    def writer = new StringWriter()
    def builder = new MarkupBuilder(writer)
    def succ
    // Define target payload mapping
    builder.'ProcessInvoiceExternallyExportReply'(partition:AribaReply.PARTITION, variant:AribaReply.VARIANT){
        'InvoiceReconciliation_ProcessIRExtValidationStatusResponseImport_Item'{
            'item'{
                'EventDetails'{
                    if (AribaReply.RETURN.item.size() == 0)
                        succ = 'Success'
                    else
                        succ = 'Failure'
                    'StatusResponse'(succ)
                }
                'UniqueName'(AribaReply.ARIBA_DOC_NO)
            }
            if(succ == 'Failure'){
                def validItems = AribaReply.RETURN.item.findAll()
                'ValidationError_ValidateErrorImport_Item'{
                    'item'{
                        'Date'(getCurrentDate())
                        'ErrorDetails' {
                            validItems.each { itemRes ->
                                'item' {
                                    'ErrorCategory'(itemRes.TYPE)
                                    'ErrorCode'(itemRes.NUNMBER)
                                    'ErrorMessage'(itemRes.MESSAGE)
                                    'LineNumber'(itemRes.ARIBA_ITEM)
                                }
                            }
                        }
                        'Id'(AribaReply.ARIBA_DOC_NO)
                    }
                }
            }
        }
    }

    // Generate output
    message.setBody(writer.toString())
    return message
}


def String getCurrentDate(){
    def date = new Date()
    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")//2019-02-22T01:22:09Z
    return sdf.format(date)
}