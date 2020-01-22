package nokia.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.xml.MarkupBuilder
import java.text.SimpleDateFormat

def Message processData(Message message) {
    // Access message body and properties
    Reader reader = message.getBody(Reader)
    //Map properties = message.getProperties()

    // Define XML parser and builder
    def AribaRequest = new XmlSlurper().parse(reader)
    def writer = new StringWriter()
    def builder = new MarkupBuilder(writer)

    // Define target payload mapping
    builder.'ns1:ARBCIG_DERIVATION'('xmlns:ns1':'urn:sap-com:document:sap:rfc:functions') {
        'ARIBA_DOC_NO'(AribaRequest.Invoice_ProcessInvoiceExtHeaderDetails_Item.item.UniqueName)
        'PARTITION'(AribaRequest.@partition)
        'VARIANT'(AribaRequest.@variant)

        def String companyCode = AribaRequest.Invoice_ProcessInvoiceExtHeaderDetails_Item.item.CompanyCode.UniqueName

        def validItems = AribaRequest.Invoice_ProcessInvoiceExtLineDetails_Item.item.LineItems.item.findAll {
            item -> (item.LineType.Category.text() == '1')
        }

        'CHECK_CODINGBLOCK'{
            validItems.each { item ->
                def lineNumber = item.NumberInCollection.text()
                'ARBCIG_BAPICOBL' {
                    'ARIBA_ITEM'(item.NumberInCollection)
                    'PSTNG_DATE'(getCurrentDate())
                    'DOC_DATE'(getCurrentDate())
                    'COMP_CODE'(companyCode)
                    def glacc = AribaRequest.Requisition_ProcessReqExtAccountingDetails_Item.item.LineItems.item.find {
                        it.Accountings.SplitAccountings.item.LineItem.NumberInCollection.text() == lineNumber}.Accountings.SplitAccountings.item.GeneralLedger.UniqueName.text()
                    'GL_ACCOUNT'(glacc)
                    'ACCT_TYPE'(item.AccountCategory.UniqueName.text())
                    'VENDOR_NO'(leadingZeros(item.Supplier.UniqueName.text(), 10))
                    def costCenter = AribaRequest.Requisition_ProcessReqExtAccountingDetails_Item.item.LineItems.item.find {
                        it.Accountings.SplitAccountings.item.LineItem.NumberInCollection.text() == lineNumber}.Accountings.SplitAccountings.item.CostCenter.UniqueName.text()
                    'COSTCENTER'(costCenter)
                    def orderID = AribaRequest.Requisition_ProcessReqExtAccountingDetails_Item.item.LineItems.item.find {
                        it.Accountings.SplitAccountings.item.LineItem.NumberInCollection.text() == lineNumber}.Accountings.SplitAccountings.item.InternalOrder.UniqueName.text()
                    'ORDERID'(orderID)
                    def wbsElement = AribaRequest.Requisition_ProcessReqExtAccountingDetails_Item.item.LineItems.item.find {
                        it.Accountings.SplitAccountings.item.LineItem.NumberInCollection.text() == lineNumber}.Accountings.SplitAccountings.item.WBSElement.UniqueName.text()
                    'WBS_ELEMENT'(wbsElement)
                    def assetMainNo = AribaRequest.Requisition_ProcessReqExtAccountingDetails_Item.item.LineItems.item.find {
                        it.Accountings.SplitAccountings.item.LineItem.NumberInCollection.text() == lineNumber}.Accountings.SplitAccountings.item.Asset.UniqueName.text()
                    'ASSETMAINNO'(assetMainNo)
                    def assetSubNo = AribaRequest.Requisition_ProcessReqExtAccountingDetails_Item.item.LineItems.item.find {
                        it.Accountings.SplitAccountings.item.LineItem.NumberInCollection.text() == lineNumber}.Accountings.SplitAccountings.item.Asset.SubNumber.text()
                    'ASSETSUBNO'(assetSubNo)
                    'FUNC_AREA_LONG'("INV")
                }
            }
        }
        'CHECK_CUSTOMERFIELDS'{
            validItems.each { item ->
                def lineNumber = item.NumberInCollection.text()
                'ARBCIG_BAPICOBL_CUST' {
                    'ARIBA_ITEM'(lineNumber)
                    'ZZEKORG'(item.PurchaseOrg.UniqueName)
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
    sdf = new SimpleDateFormat("yyyyMMdd")
    return sdf.format(date)
}

def String leadingZeros(String s, int length) { //10
    if (s.length() >= length) return s;
    else return String.format("%0" + (length-s.length()) + "d%s", 0, s);
}