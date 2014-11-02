import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class ExampleClient {
  public static void main(String[] args) throws IOException {
    Configuration config = HBaseConfiguration.create();

    // Create table
    HBaseAdmin admin = new HBaseAdmin(config);
    HTableDescriptor htd = new HTableDescriptor("test");
    HColumnDescriptor hcd = new HColumnDescriptor("data");
    htd.addFamily(hcd);
    admin.createTable(htd);
    byte[] tablename = htd.getName();
    HTableDescriptor[] tables = admin.listTables();
    if (tables.length != 1 && Bytes.equals(tablename, tables[0].getName())) {
      throw new IOException("Failed create of table");
    }

    // Run some operations -- three puts, a get, and a scan -- against the table.
    HTable table = new HTable(config, tablename);
    for (int i = 1; i <= 3; i++) {
      byte[] row = Bytes.toBytes("row" + i);
      Put put = new Put(row);
      byte[] columnFamily = Bytes.toBytes("data");
      byte[] qualifier = Bytes.toBytes(String.valueOf(i));
      byte[] value = Bytes.toBytes("value1");
      put.add(columnFamily, qualifier, value);
      table.put(put);
    }

    Get get = new Get(Bytes.toBytes("row1"));
    Result result = table.get(get);
    System.out.println("Get: " + result);

    Scan scan = new Scan();
    ResultScanner scanner = table.getScanner(scan);
    try {
      for (Result scannerResult: scanner) {
        System.out.println("Scan: " + scannerResult);
      }
    } finally {
      scanner.close();
    }

    // Disable then drop the table
    admin.disableTable(tablename);
    admin.deleteTable(tablename);
  }
}
