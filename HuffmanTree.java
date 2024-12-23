import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Implementation of a Huffman tree that determines byte frequency.
 * 
 * @author Griffin Moran
 * @version 1.0
 */
public class HuffmanTree implements Comparable<HuffmanTree> {
  private int frequency;
  private HuffInternal root;
  private HuffInternal left = null;
  private HuffInternal right = null;
  private HashMap<Byte, String> mappedVals = new HashMap<>(); 
  
  /**
   * Constructor HuffmanTree with a HuffLeaf root.
   * Serves as a leaf node of a full tree.
   * 
   * @param element The byte
   * @param frequency The frequency of the byte
   */
  HuffmanTree(byte element, int frequency) {
    root = new HuffLeaf(element, frequency);
    this.frequency = frequency;
  }
  
  /**
   * Constructor for an HuffmanTree with a HuffInternal root.
   * 
   * @param left The left child
   * @param right The right child
   * @param frequency The sum of the frequencies of the sub trees
   */
  HuffmanTree(HuffInternal left, HuffInternal right, int frequency) {
    root = new HuffInternal(left, right, frequency);
    this.left = left;
    this.right = right;
    this.frequency = frequency;
  }
  
  /**
   * Construct the HuffmanTree using the process outlined in OpenDSA.
   * 
   * @param frequencies The HashMap linking bytes their frequency in a file
   * @return The filled HuffmanTree
   */
  HuffmanTree buildTree(HashMap<Byte, Integer> frequencies) {
    if (frequencies.keySet().size() == 0) {
      return new HuffmanTree((byte) 0, 0);
    }
    if (frequencies.keySet().size() == 1) {
      Byte key = (Byte) frequencies.keySet().toArray()[0];
      HuffmanTree ht = new HuffmanTree(key, frequencies.get(key));
      return new HuffmanTree(null, ht.root, frequencies.get(key));
    }
    HuffmanHeap hh = new HuffmanHeap(frequencies);
    HuffmanTree combo = null;
    
    while (hh.size() > 1) {
      HuffmanTree left =  hh.poll();
      HuffmanTree right = hh.poll();
      int sum = left.frequency() + right.frequency();
      combo = left.compareTo(right) <= 0 ? (new HuffmanTree(left.root, right.root, sum)) :
        (new HuffmanTree(right.root, left.root, sum));
      hh.add(combo);
    }
    return combo;
  }
  
  /**
   * Create a byte mapping for a the current tree and place them into a Hashmap parameter
   * to be used for the encoding/decoding process.
   * 
   * @param root The base node to avoid aliasing so this root never changes
   * @param str The string being built recursively, 0 indicates left, 1 indicates right
   */
  public void map(HuffInternal root, String str) { 
    if (root == null) {
      return;
    }  
    if (root.isLeaf()) {
      HuffLeaf leaf = (HuffLeaf) root;
      mappedVals.put(leaf.element, str);
    }  
    map(root.left(), str + "0"); 
    map(root.right(), str + "1");
  }
  
  /**
   * Parse through a sequence of bits to recreate a previously compressed file. 
   * 
   * @param bs The BitSequence representing the compressed file
   * @param filename The name of the new file to create
   * @return The newly uncompressed file in original format
   */
  public File decode(BitSequence bs, String filename) throws IOException {
    HuffInternal base = new HuffInternal(root);   
    File output = new File(filename);
    FileOutputStream fos = new FileOutputStream(output);
    
    int i = 0;
    while (i < bs.length()) {
      if (bs.getBit(i) == 0 && !base.isLeaf()) {
        base = base.left();
      } else if (bs.getBit(i) == 1 && !base.isLeaf()) {
        base = base.right();
      }
      if (base.isLeaf()) {
        HuffLeaf hl = (HuffLeaf) base;
        fos.write(hl.element);
        base = new HuffInternal(root);
      }
      i++;
    }
    fos.close();
    return output;  
  }

  /**
   * Used to break frequency ties when traversing the tree, 
   * compares weights based on the deepest leaves of each tree's left child.
   * 
   * @param other The tree to compare to
   */
  public int compareTo(HuffmanTree other) {
    if (root.frequency() <  other.root.frequency()) {
      return -1;
    } else if (root.frequency() == other.root.frequency()) {
      HuffInternal cur = this.root;
      HuffInternal comp = other.root;
      
      while (!cur.isLeaf() && !comp.isLeaf()) {
        cur = cur.left();
        comp = comp.left();
      }
      if (cur.isLeaf() != comp.isLeaf()) {
        if (cur.isLeaf()) {
          return -1;
        }
        return 1;
      }
      
      HuffLeaf current = (HuffLeaf) cur;
      HuffLeaf compare = (HuffLeaf) comp;
      return current.compareTo(compare);
    } else {
      return 1;
    }
  }
  
  /**
   * Helper method that returns the frequency of the tree.
   * 
   * @return the frequency
   */
  public int frequency() {
    return frequency;
  }
  
  /**
   * Helper method that returns the left child.
   * 
   * @return The left child node.
   */
  public HuffInternal getLeft() {
    return left;
  }
  
  /**
   * Helper method that returns the right child.
   * 
   * @return The right child node.
   */
  public HuffInternal getRight() {
    return right;
  }
  
  /**
   * Helper method that returns the root.
   * 
   * @return The root
   */
  public HuffInternal getRoot() {
    return root;
  }
  
  /**
   * Helper method that returns the Binary value mapping.
   * 
   * @return The map
   */
  @SuppressWarnings("rawtypes")
  public HashMap getMappedVals() {
    return mappedVals;
  }
  
  /**
   * Internal class that allows for easy HuffmanTree heap construction.
   */
  class HuffmanHeap extends PriorityQueue<HuffmanTree> {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a HuffmanHeap that serves as a priority queue and adds each
     * frequency to the heap as a HuffmanTree with a leaf root.
     * 
     * @param frequencies The fequencies to use when constructing the heap
     */
    HuffmanHeap(HashMap<Byte, Integer> frequencies) {  
      Set<Byte> freqs = frequencies.keySet(); 
      for (Byte element :  freqs) {
        this.add(new HuffmanTree(element, frequencies.get(element)));
      }
    }
  }
  
  /**
   * Huffman tree internal node implementation.
   */
  class HuffInternal {
    HuffInternal left;
    HuffInternal right;
    int frequency;
    
    /**
     * Default constructor.
     */
    HuffInternal() {
      this.left = null;
      this.right = null;
      this.frequency = 0;
    }
    
    /**
     * Copy constructor.
     * 
     * @param internal The node to copy
     */
    HuffInternal(HuffInternal internal) {
      this.left = internal.left;
      this.right = internal.right;
      this.frequency = internal.frequency;
    }

    /**
     * Regular constructor for an internal node.
     * 
     * @param left The left child if one exists
     * @param right The right child if one exists
     * @param frequency The frequency of the node
     */
    HuffInternal(HuffInternal left, HuffInternal right, int frequency) {
      this.left = left;
      this.right = right; 
      this.frequency = frequency;
    }
    
    /**
     * Return the left child node.
     */
    HuffInternal left() {
      return this.left;
    }
    
    /**
     * Return the right child node.
     */
    HuffInternal right() {
      return this.right;
    }
    
    /**
     * Return the frequency.
     */
    int frequency() {
      return this.frequency;
    }
    
    /**
     * Infix method created to help during testing.
     * 
     * @return A string representation of each leaf node in the tree in order from left to right
     */
    String infix() {
      String left = this.left().infix();
      String right = this.right().infix();
      return left + right;
    }
    
    /**
     * Always false.
     */
    boolean isLeaf() {
      return false;
    }
    
  }
  
  /**
   * Huffman tree leaf node implementation.
   */
  class HuffLeaf extends HuffInternal {
    private byte element;
    
    /**
     * Leaf Node constructor.
     * 
     * @param element The element represented by the node
     * @param frequency The frequency of the node
     */
    HuffLeaf(byte element, int frequency) {
      super(null, null, frequency);
      this.element = element;
    }
    
    /**
     * Return the byte element represented by the node.
     */
    byte element() {
      return element;
    }
    
    /**
     * Always true.
     */
    boolean isLeaf() {
      return true;
    }
    
    /**
     * Overriden infix used by the super class.
     */
    String infix() {
      String str = "";
      str += (char) element;
      if (str.equals("\n")) {
        return "Byte " + element + " /n" + "-" + frequency + " ";
      }
      return "Byte " + element + " Char " + str + " Freq " + frequency + " |";
    }
    
    /**
     * Always null since this is a leaf.
     */
    HuffInternal left() {
      return null;
    }
    
    /**
     * Always null since this is a leaf.
     */
    HuffInternal right() {
      return null;
    }

    /**
     * Compares HuffLeaves elements to break ties.
     * 
     * @param other The leaf to compare to
     */
    public int compareTo(HuffLeaf other) {
      Byte cur = (Byte) this.element;
      Byte comp = (Byte) other.element;  
      return Byte.compare(cur, comp);
    }
  }
}
