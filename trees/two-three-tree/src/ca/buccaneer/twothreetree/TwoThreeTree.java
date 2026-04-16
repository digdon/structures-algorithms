package ca.buccaneer.twothreetree;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public class TwoThreeTree<T extends Comparable<T>> {

    private Node root = null;

    private BiFunction<T, T, T> mergeCheck;  // Method to call when items need to be merged together

    public TwoThreeTree(BiFunction<T, T, T> mergeCheck) {
        this.mergeCheck = mergeCheck;
    }
    
    private static final String SPACER = "    ";
    
    public void display() {
        displayNode(root, "");
    }
    
    private void displayNode(Node node, String spacer) {
        if (node == null) {
            return;
        }
        
        System.out.println(spacer + node);
        
        System.out.println(spacer + "LM: " + node.getLeftMax());
        System.out.println(spacer + "MM: " + node.getMiddleMax());

        System.out.println(spacer + "left: ");
        displayNode(node.getLeftChild(), spacer + SPACER);

        System.out.println(spacer + "middle: ");
        displayNode(node.getMiddleChild(), spacer + SPACER);

        System.out.println(spacer + "right: ");
        displayNode(node.getRightChild(), spacer + SPACER);
    }
    
    public void add(T item) {
        if (root == null) {
            root = new Node(item);
        } else {
            Node newRoot = insertNode(root, item);
            
            if (newRoot != null) {
                root = newRoot;
            }
        }
    }
    
    private Node insertNode(Node current, T item) {
        Node pushUpNode = null;
        
        if (current.isLeaf() || current.childrenAreLeaves()) {
            // Found the node/leaf to do the insertion
            
            // Is the item already in here?
            if (itemFoundInLeaf(current, item)) {
                if (mergeCheck != null) {
                    processItemMerge(current, item);
                }
                return null;
            } else {
                if (current.isFull() == false) {
                    // Leaf has less than 3 children
                    placeInLeaf(current, item);
                } else {
                    pushUpNode = splitNode(current, item);
                }
            }
        } else {
            // Not deep enough - continue traversing
            if (item.compareTo(current.getLeftMax()) <= 0) {
                // Follow the left child
                pushUpNode = insertNode(current.getLeftChild(), item);
                
                if (pushUpNode != null) {
                    if (current.isFull() == false) {
                        current.setRightChild(current.getMiddleChild());
                        current.setMiddleChild(pushUpNode.getMiddleChild());
                        current.setLeftChild(pushUpNode.getLeftChild());
                        pushUpNode = null;
                    } else {
                        Node parent = new Node();
                        Node left = new Node();
                        Node middle = new Node();

                        left.setLeftChild(pushUpNode.getLeftChild());
                        left.setMiddleChild(pushUpNode.getMiddleChild());
                        left.setLeftMax(left.getLeftChild().maxValue());
                        left.setMiddleMax(left.getMiddleChild().maxValue());
                        
                        middle.setLeftChild(current.getMiddleChild());
                        middle.setMiddleChild(current.getRightChild());
                        middle.setLeftMax(middle.getLeftChild().maxValue());
                        middle.setMiddleMax(middle.getMiddleChild().maxValue());
                        
                        parent.setLeftChild(left);
                        parent.setLeftMax(parent.getLeftChild().maxValue());
                        parent.setMiddleChild(middle);
                        parent.setMiddleMax(parent.getMiddleChild().maxValue());
                        pushUpNode = parent;
                    }
                }
            } else if (item.compareTo(current.getMiddleMax()) <= 0 || current.getRightChild() == null) {
                // Follow the middle child
                pushUpNode = insertNode(current.getMiddleChild(), item);
                
                if (pushUpNode != null) {
                    if (current.isFull() == false) {
                        current.setMiddleChild(pushUpNode.getLeftChild());
                        current.setRightChild(pushUpNode.getMiddleChild());
                        pushUpNode = null;
                    } else {
                        Node parent = new Node();
                        Node left = new Node();
                        Node middle = new Node();
                        
                        left.setLeftChild(current.getLeftChild());
                        left.setMiddleChild(pushUpNode.getLeftChild());
                        left.setLeftMax(left.getLeftChild().maxValue());
                        left.setMiddleMax(left.getMiddleChild().maxValue());
                        
                        middle.setLeftChild(pushUpNode.getMiddleChild());
                        middle.setMiddleChild(current.getRightChild());
                        middle.setLeftMax(middle.getLeftChild().maxValue());
                        middle.setMiddleMax(middle.getMiddleChild().maxValue());

                        parent.setLeftChild(left);
                        parent.setLeftMax(parent.getLeftChild().maxValue());
                        parent.setMiddleChild(middle);
                        parent.setMiddleMax(parent.getMiddleChild().maxValue());
                        pushUpNode = parent;
                    }
                }
            } else {
                // Follow the right child
                pushUpNode = insertNode(current.getRightChild(), item);
                
                if (pushUpNode != null) {
                    Node parent = new Node();
                    current.setRightChild(null);
                    parent.setLeftChild(current);
                    parent.setLeftMax(parent.getLeftChild().maxValue());
                    parent.setMiddleChild(pushUpNode);
                    parent.setMiddleMax(parent.getMiddleChild().maxValue());
                    pushUpNode = parent;
                }
            }
        }
        
        // Fix max values along the way
        current.setLeftMax(current.getLeftChild().maxValue());
        current.setMiddleMax(current.getMiddleChild().maxValue());
        
        return pushUpNode;
    }
    
    private boolean itemFoundInLeaf(Node node, T item) {
        if (node.isLeaf()) {
            return (item.compareTo(node.getLeftMax()) == 0);
        } else { // if (node.childrenAreLeaves()) {
            return ((node.getLeftChild() != null && item.compareTo(node.getLeftChild().getLeftMax()) == 0)
                        || (node.getMiddleChild() != null && item.compareTo(node.getMiddleChild().getLeftMax()) == 0)
                        || (node.getRightChild() != null && item.compareTo(node.getRightChild().getLeftMax()) == 0));
        }
    }
    
    private void placeInLeaf(Node leaf, T item) {
        // Figure out where this belongs - left, middle, or right?
        if (item.compareTo(leaf.getLeftMax()) < 0) {
            // Item is less that current leaf left - move everything to the right
            leaf.setRightChild(leaf.getMiddleChild());
            leaf.setMiddleMax(leaf.getLeftMax());
            leaf.setLeftMax(item);
        } else {
            if (leaf.getMiddleChild() == null) {
                // Item automatically becomes middle
                leaf.setMiddleMax(item);
            } else if (item.compareTo(leaf.getMiddleMax()) < 0) {
                // Item is less than current leaf middle - move middle to the right
                leaf.setRightChild(leaf.getMiddleChild());
                leaf.setMiddleMax(item);
            } else {
                // Item automatically goes to right
                leaf.setRightChild(new Node(item));
            }
        }
        
        // Fix the children
        leaf.setLeftChild(new Node(leaf.getLeftMax()));
        leaf.setMiddleChild(new Node(leaf.getMiddleMax()));
    }
    
    private Node splitNode(Node node, T item) {
        Node parent = new Node();
        Node left = new Node();
        Node middle = new Node();
        Node k = new Node(item);
        
        // Figure out which two children go to new left and which go to new middle
        if (item.compareTo(node.getMiddleMax()) < 0) {
            // New item less than middle, so item and left to go new left, middle and right go to new middle
            middle.setLeftChild(node.getMiddleChild());
            middle.setMiddleChild(node.getRightChild());
            
            // Left and new go to new left
            if (item.compareTo(node.getLeftMax()) < 0) {
                // New item is smallest
                left.setMiddleChild(node.getLeftChild());
                left.setLeftChild(k);
            } else {
                left.setLeftChild(node.getLeftChild());
                left.setMiddleChild(k);
            }
        } else  {
            // New item greater than middle, so left and middle go to new left, item and right move to new middle
            left.setLeftChild(node.getLeftChild());
            left.setMiddleChild(node.getMiddleChild());
            
            if (item.compareTo(node.getRightChild().maxValue()) > 0) {
                // New item is largest
                middle.setLeftChild(node.getRightChild());
                middle.setMiddleChild(k);
            } else {
                middle.setLeftChild(k);
                middle.setMiddleChild(node.getRightChild());
            }
        }

        left.setLeftMax(left.getLeftChild().maxValue());
        left.setMiddleMax(left.getMiddleChild().maxValue());
        
        middle.setLeftMax(middle.getLeftChild().maxValue());
        middle.setMiddleMax(middle.getMiddleChild().maxValue());
        
        parent.setLeftChild(left);
        parent.setLeftMax(left.maxValue());
        parent.setMiddleChild(middle);
        parent.setMiddleMax(middle.maxValue());
        
        return parent;
    }

    private void processItemMerge(Node node, T item) {
        if (node.isLeaf()) {
            if (mergeCheck != null) {
                T apply = mergeCheck.apply(node.leftMax, item);
                node.setLeftMax(apply);
            }
        } else {
            if (item.compareTo(node.getLeftMax()) == 0) {
                processItemMerge(node.getLeftChild(), item);
            } else if (item.compareTo(node.getMiddleMax()) == 0) {
                processItemMerge(node.getMiddleChild(), item);
            } else {
                processItemMerge(node.getRightChild(), item);
            }
        }
    }
    
    public T find(T item) {
        return findItemI(root, item);
    }
    
    private T findItemI(Node node, T item) {
        if (node == null) {
            return null;
        }
        
        if (node.isLeaf()) {
            if (item.compareTo(node.getLeftMax()) == 0) {
                return node.getLeftMax();
            } else {
                return null;
            }
        } else {
            // Not a leaf, need to walk it
            if (item.compareTo(node.getLeftMax()) <= 0) {
                // Search item less than left max, follow left
                return findItemI(node.getLeftChild(), item);
            } else if (item.compareTo(node.getMiddleMax()) <= 0) {
                // Search item less than middle max, follow left
                return findItemI(node.getMiddleChild(), item);
            } else {
                return findItemI(node.getRightChild(), item);
            }
        }
    }
    
    public List<T> flattenToList() {
        List<T> flattenedList = new LinkedList<>();
        inOrderAddToList(root, flattenedList);
        
        return flattenedList;
    }
    
    private void inOrderAddToList(Node current, List<T> list) {
        if (current != null) {
            if (current.isLeaf()) {
                list.add(current.getLeftMax());
            } else {
                inOrderAddToList(current.getLeftChild(), list);
                inOrderAddToList(current.getMiddleChild(), list);
                inOrderAddToList(current.getRightChild(), list);
            }
        }
    }
    
    private class Node {

        private T leftMax;
        private T middleMax;
        
        private Node leftChild;
        private Node middleChild;
        private Node rightChild;
        
        public Node() {
        }
        
        public Node(T item) {
            this.leftMax = item;
        }

        public T getLeftMax() {
            return leftMax;
        }

        public void setLeftMax(T leftMax) {
            this.leftMax = leftMax;
        }

        public T getMiddleMax() {
            return middleMax;
        }

        public void setMiddleMax(T middleMax) {
            this.middleMax = middleMax;
        }

        public Node getLeftChild() {
            return leftChild;
        }

        public void setLeftChild(Node leftChild) {
            this.leftChild = leftChild;
        }

        public Node getMiddleChild() {
            return middleChild;
        }

        public void setMiddleChild(Node middleChild) {
            this.middleChild = middleChild;
        }

        public Node getRightChild() {
            return rightChild;
        }

        public void setRightChild(Node rightChild) {
            this.rightChild = rightChild;
        }

        public boolean isLeaf() {
            return (leftChild == null && middleChild == null && rightChild == null);
        }
        
        public boolean childrenAreLeaves() {
            return ((leftChild == null || leftChild.isLeaf())
                        && (middleChild == null || middleChild.isLeaf())
                        && (rightChild == null || rightChild.isLeaf()));
        }
        
        public boolean isFull() {
            return (rightChild != null);
        }

        public T maxValue() {
            Node temp = this;
            
            while (temp.rightChild != null) {
                temp = temp.rightChild;
            }
            
            if (temp.middleMax != null) {
                return temp.middleMax;
            } else {
                return temp.leftMax;
            }
        }
    }
}
