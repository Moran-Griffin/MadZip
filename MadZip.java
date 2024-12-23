import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.util.HashMap;

/**
 * Class that provides functionality to zip and unzip files using Huffman Trees and HashMaps.
 * 
 * @author Griffin Moran
 */
public class MadZip {
  
  /**
   * Compresses the given file and stores the compression at the provided destination
   * will override the current destination file if there is one.
   * 
   * @param current The file to compress
   * @param destination The destination to save the zipped file to
   */
  public static void zip(File current, File destination) throws IOException {  
    HuffmanProducer output = new HuffmanProducer(current);
    HuffmanSave hs = output.generateBytes(); 
    write(hs, destination.getName());
  }
  
  /**
   * Serialization helper method used to write a HuffmanSave to a provided File destination.
   * 
   * @param hs The HuffmanSave to serailize
   * @param destination The location to write to
   */
  private static void write(HuffmanSave hs, String destination) throws IOException {
    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(destination));

    out.writeObject(hs);
    out.flush();
    out.close();
  }
  
  /**
   * Unzip the provided file and save it to the provided destination
   * will override the current destination file if there is one.
   * 
   * @param zipped The currently compressed file
   * @param destination The destination to save the unzipped file to
   */
  public static void unzip(File zipped, File destination) 
      throws IOException, ClassNotFoundException {
    HuffmanSave hs = read(zipped);
    HashMap<Byte, Integer> frequencies = hs.getFrequencies();
    HuffmanTree tree = new HuffmanTree((byte) -1, -1);
    tree = tree.buildTree(frequencies);
    tree.decode(hs.getEncoding(), destination.getName());
  }
  
  /**
   * Helper method that attempts to deserialize a HuffmanSave from a file.
   * 
   * @param zipped The file to read from
   * @return The deserialized HuffmanSave
   */
  private static HuffmanSave read(File zipped) throws IOException, ClassNotFoundException {
    ObjectInputStream input = new ObjectInputStream(new FileInputStream(zipped));
    HuffmanSave hs;
    hs = (HuffmanSave) input.readObject();
    input.close();
    return hs;
  }
}
