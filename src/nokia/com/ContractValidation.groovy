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
        'ARIBA_DOC_NO'(AribaRequest.ContractRequest_ContractRequestExternalHeaderDetails_Item.item.UniqueName)
        'PARTITION'(AribaRequest.@partition)
        'VARIANT'(AribaRequest.@variant)
        'SOURCE'("BUY")
        def String venderNO = AribaRequest.ContractRequest_ContractRequestExternalHeaderDetails_Item.item.Supplier.UniqueName
        def String companyCode = AribaRequest.ContractRequest_ContractRequestExternalHeaderDetails_Item.item.CompanyCode.UniqueName
       def String purchaseOrg = AribaRequest.ContractRequest_ContractRequestExternalHeaderDetails_Item.item.PurchaseOrg.UniqueName

        def validItems = AribaRequest.ContractRequest_ContractRequestExternalAccountingDetails_Item.item.LineItems.item.findAll()

        'CHECK_CODINGBLOCK'{
            validItems.each { item ->
                def lineNumber = item.Accountings.SplitAccountings.item.LineItem.NumberInCollection.text()
                //'ARBCIG_BAPICOBL' {
                'ITEM' {
                    'ARIBA_ITEM'(lineNumber)
                    'PSTNG_DATE'(getCurrentDate())
                    'DOC_DATE'(getCurrentDate())
                    'COMP_CODE'(companyCode)
                    'GL_ACCOUNT'(item.GeneralLedger.UniqueName.text())
                    def accType = AribaRequest.ContractRequest_ContractRequestExternalLineDetails_Item.item.LineItems.item.find {
                        it.NumberInCollection.text() == lineNumber}.AccountCategory.UniqueName.text()
                    'ACCT_TYPE'(accType)
                    'VENDOR_NO'(leadingZeros(venderNO,10))
                    'COSTCENTER'(item.Accountings.SplitAccountings.item.CostCenter.UniqueName.text())
                    'ORDERID'(item.Accountings.SplitAccountings.item.InternalOrder.UniqueName.text())
                    'WBS_ELEMENT'(item.Accountings.SplitAccountings.item.WBSElement.UniqueName.text())
                    'ASSETMAINNO'(item.Accountings.SplitAccountings.item.Asset.UniqueName.text())
                    'ASSETSUBNO'(item.Accountings.SplitAccountings.item.Asset.SubNumber.text())
                    'FUNC_AREA_LONG'("CON")
                }
            }
        }
        'CHECK_CUSTOMERFIELDS'{
            validItems.each { item ->
                def lineNumber = item.Accountings.SplitAccountings.item.LineItem.NumberInCollection.text()
                'ITEM' {
               // 'ARBCIG_BAPICOBL_CUST' {
                    'ARIBA_ITEM'(lineNumber)
                    'ZZEKORG'(purchaseOrg)
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
