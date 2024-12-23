import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Class used to de-couple the zipping process. This class handles a majority of
 * the tasks required to complete before creating a HuffmanSave.
 * 
 * @author Griffin Moran
 */
public class HuffmanProducer implements Serializable {
  private static final long serialVersionUID = 3L;
  
  HashMap<Byte, Integer> frequencies;
  File file;
  HashMap<Byte, String> mapping = new HashMap<>();
  private HuffmanTree tree = new HuffmanTree((byte) -1, -1);
  
  /**
   * Construct a HuffmanProducer that parses a file, once to calculate frequencies
   * and a second time to create a HuffmanTree.
   * 
   * @param file The file to be analyzed
   */
  public HuffmanProducer(File file) throws IOException {
    this.file = file;
    frequencies = new HashMap<>();
    calculateFrequency();
    constructTree();
  }
  
  /**
   * Helper method that parses the file and calculates the frequency for each byte,
   * adding each to a variable.
   */
  private void calculateFrequency() throws IOException { //what to do about \n
    FileInputStream fis = new FileInputStream(file);
    BufferedInputStream bis = new BufferedInputStream(fis);
    
    int cur;
    while ((cur = bis.read()) != -1) {  
      byte byteCur = (byte) cur;     
      if (!frequencies.containsKey(byteCur)) {
        frequencies.put(byteCur, 1);
      } else {
        frequencies.put(byteCur,  frequencies.get(byteCur) + 1);
      }
    }
    bis.close();
  }
  
  /**
   * Helper method that constructs a HuffmanTree, creates a mapping
   * to each byte in the file, and assigns that mapping to a variable.
   */
  @SuppressWarnings("unchecked")
  private void constructTree() throws IOException {  
    tree = tree.buildTree(frequencies);
    tree.map(tree.getRoot(), "");
    mapping = tree.getMappedVals();  
  }
  
  /**
   * Generates a BitSequence for the owning file and frequency HashMap, 
   * the BitString is then used to create a HuffmanSave.
   */
  public HuffmanSave generateBytes() throws IOException {
    FileInputStream fis = new FileInputStream(file);
    BufferedInputStream bis = new BufferedInputStream(fis);
    BitSequence encoding = new BitSequence();
    
    int cur;
    while ((cur = bis.read()) != -1) {
      byte  byteCur = (byte) cur;   
      String bit = mapping.get(byteCur);
      encoding.appendBits(bit);
    }
    bis.close();
    
    return new HuffmanSave(encoding, frequencies);
  }  
  
  /**
   * Returns the HuffmanTree corresponding to the attributes of this class.
   * This is a helper method primarily used for testing purposes.
   * 
   * @return The HuffmanTree
   */
  public HuffmanTree getTree() {
    return tree;
  }
}
