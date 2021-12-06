import org.apache.spark.sql.functions._


//  "fileName": "order_data.csv",
//         "contanierName": "staging",
//         "storageAccountName": "storageaccountdemosm ",
//         "sasKey": "?sv=2020-08-04&ss=bfqt&srt=sco&sp=rwdlacupitfx&se=2021-11-15T05:07:14Z&st=2021-11-10T21:07:14Z&spr=https&sig=4bakMzMq01LH5WubFnzlRT8%2BUF%2FbCfsDIAgTIAzU4IM%3D",
//         "outputFileName": "output_file.csv"

val fileName = dbutils.widgets.get("fileName")
val containerName = dbutils.widgets.get("contanierName")
val storageAccountName = dbutils.widgets.get("storageAccountName")
val sas = dbutils.widgets.get("sasKey")
val outputFileName = dbutils.widgets.get("outputFileName")
// val fileName = "order_data.csv"
// val containerName = "staging"
// val storageAccountName = "storageaccountdemosm"
// val outputFileName = "output_file.csv"
// val sas = "?sv=2020-08-04&ss=bfqt&srt=sco&sp=rwdlacupitfx&se=2021-11-15T05:07:14Z&st=2021-11-10T21:07:14Z&spr=https&sig=4bakMzMq01LH5WubFnzlRT8%2BUF%2FbCfsDIAgTIAzU4IM%3D"

val wasbs_path = s"wasbs://$containerName@$storageAccountName.blob.core.windows.net" 
val config = f"fs.azure.sas.$containerName.$storageAccountName.blob.core.windows.net"

try {
dbutils.fs.mount(
  source=wasbs_path,
  mountPoint=f"/mnt/$containerName",
  extraConfigs=Map(config -> sas))
}
catch {
  case _:Throwable => {dbutils.fs.unmount(f"/mnt/$containerName") ; println("caught exception")}
}
finally {
  dbutils.fs.mount(
  source=wasbs_path,
  mountPoint=f"/mnt/$containerName",
  extraConfigs=Map(config -> sas))
}

val invoiceDF = spark.read.
      format("csv").
      option("header", true).
      option("inferSchema", true).
      option("path", f"/mnt/$containerName/$fileName").
      load()

invoiceDF.createOrReplaceTempView("invoices")

val aggregatedDF = spark.sql("""
SELECT 
    Country, 
    CustomerID, 
    InvoiceNo, 
    SUM(Quantity) AS Total_Quantity,
    SUM(UnitPrice) AS Total_Unit_Price
FROM invoices
GROUP BY Country, CustomerID, InvoiceNo
HAVING Total_Quantity > 0
ORDER BY Total_Quantity DESC, Total_Unit_Price DESC
""")
aggregatedDF.write.format("csv").mode("overwrite").option("path", f"/mnt/${containerName}/${outputFileName}").save()
