package com.leoeh.prefixtree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Provides an implementation for a prefix tree, that is a more space efficient variant of a trie.
 * <p>
 * Allows insertion of elements along with words, which can later be used to retrieve the elements.
 * Enables efficient element search by word and allows users to remove elements, while optimizing
 * the tree structure by removing unused words and merging or deleting obsolete nodes.
 *
 * @param <E> the type of the elements referenced by this prefix tree
 */
public class PrefixTree<E> {

    private Map<E, Set<Node<E>>> elements;
    private Node<E> root;

    /**
     * Creates a new prefix tree.
     */
    public PrefixTree() {
        this.elements = new HashMap<>();
    }

    /**
     * Returns the number of elements stored in this prefix tree.
     *
     * @return the number of elements stored in this prefix tree
     */
    public int getSize() {
        return this.elements.keySet().size();
    }

    /**
     * Returns the number of nodes present in this prefix tree.
     *
     * @return the number of nodes present in this prefix tree
     */
    public int getNodeCount() {
        final Iterator<Node<E>> nodeIterator = new NodeIterator(this.root);
        int nodeCount = 0;

        while (nodeIterator.hasNext()) {
            nodeIterator.next();
            nodeCount++;
        }

        return nodeCount;
    }

    /**
     * Inserts the specified word into the prefix tree and associates the given element with the
     * word, if it is not already associated with that word (optional operation). The element can
     * later be retrieved by searching for the word.
     * <p>
     * Requires the word to be at least one character long.
     *
     * @param word    the word used to find the element
     * @param element the element to be associated with the word
     * @throws IllegalArgumentException if the specified word is null or empty or if the provided
     *                                  element is null
     */
    public void insert(final String word, final E element) {
        if (word == null) {
            throw new IllegalArgumentException("Word cannot be null");
        }
        if (word.isEmpty()) {
            throw new IllegalArgumentException("Word cannot be empty");
        }
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }

        final String lowerCaseWord = word.toLowerCase();

        if (this.root == null) {
            this.root = new Node<>();
            this.root.setPrefix(lowerCaseWord);
            this.root.addElement(element);
            this.addElementMapping(element, this.root);
        } else {
            this.insertRecursively(this.root, lowerCaseWord, element);
        }
    }

    /**
     * Searches for the specified word in this prefix tree and returns a set containing all elements
     * associated with that word.
     *
     * @param word the word whose associated elements are to be found
     * @return the set of elements associated with the specified word
     * @throws IllegalArgumentException if the specified word is null or empty
     */
    public Set<E> find(final String word) {
        if (word == null) {
            throw new IllegalArgumentException("Word cannot be null");
        }
        if (word.isEmpty()) {
            throw new IllegalArgumentException("Word cannot be empty");
        }

        if (this.root == null) {
            return new HashSet<>();
        }

        return this.findRecursively(this.root, word.toLowerCase());
    }

    /**
     * Searches for all words, which begin with the specified prefix, and returns a set containing
     * all elements associated with those words.
     *
     * @param prefix the prefix of the words whose associated elements are to be found
     * @return the set of elements associated with words beginning with the specified prefix
     * @throws IllegalArgumentException if the specified prefix is null or empty
     */
    public Set<E> findByPrefix(final String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null");
        }
        if (prefix.isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be empty");
        }

        if (this.root == null) {
            return new HashSet<>();
        }

        return this.findRecursivelyByPrefix(this.root, prefix.toLowerCase());
    }

    /**
     * Removes the provided element from this prefix tree, if present (optional operation). Also
     * removes all words which point exclusively to this element and optimizes the tree structure by
     * merging nodes and removing unnecessary nodes if possible.
     *
     * @param element the element to be removed
     * @throws IllegalArgumentException if the provided element is null
     */
    public void remove(final E element) {
        if (element == null) {
            throw new IllegalArgumentException();
        }

        if (!this.elements.containsKey(element)) {
            return;
        }

        final Set<Node<E>> nodesReferencingElement = this.elements.get(element);
        for (final Node<E> node : nodesReferencingElement) {
            node.removeElement(element);
            this.cleanUpRecursively(node);
        }

        this.elements.remove(element);
    }

    /**
     * Removes all entries from this prefix tree. Ensures that this tree is empty.
     */
    public void clear() {
        this.elements = new HashMap<>();
        this.root = null;
    }

    private void insertRecursively(final Node<E> node, final String word, final E element) {
        final String nodePrefix = node.getPrefix();

        if (nodePrefix.equals(word)) {
            node.addElement(element);
            this.addElementMapping(element, node);
            return;
        }

        final String greatestCommonPrefix = this.getGreatestCommonPrefix(nodePrefix, word);
        final String nodeSuffix = nodePrefix.substring(greatestCommonPrefix.length());
        final String wordSuffix = word.substring(greatestCommonPrefix.length());

        if (greatestCommonPrefix.length() < nodePrefix.length()) {
            final Node<E> newNode1 = new Node<>();

            newNode1.setPrefix(greatestCommonPrefix);
            node.setPrefix(nodeSuffix.substring(1));

            if (node == this.root) {
                this.root = newNode1;
            } else {
                final Node<E> parent = node.getParent();
                final char parentEdgeLabel = node.getParentEdgeLabel();

                parent.setChild(parentEdgeLabel, newNode1);
            }

            newNode1.setChild(nodeSuffix.charAt(0), node);

            if (!wordSuffix.isEmpty()) {
                final Node<E> newNode2 = new Node<>();

                newNode2.setPrefix(wordSuffix.substring(1));
                newNode2.addElement(element);
                this.addElementMapping(element, newNode2);

                newNode1.setChild(wordSuffix.charAt(0), newNode2);
            } else {
                newNode1.addElement(element);
                this.addElementMapping(element, newNode1);
            }

            return;
        }

        if (node.hasChild(wordSuffix.charAt(0))) {
            this.insertRecursively(node.getChild(wordSuffix.charAt(0)),
                    wordSuffix.substring(1),
                    element);
            return;
        }

        final Node<E> newNode2 = new Node<>();

        newNode2.setPrefix(wordSuffix.substring(1));
        newNode2.addElement(element);
        this.addElementMapping(element, newNode2);

        node.setChild(wordSuffix.charAt(0), newNode2);
    }

    private Set<E> findRecursively(final Node<E> node, final String word) {
        final String nodePrefix = node.getPrefix();

        if (nodePrefix.equals(word)) {
            return new HashSet<>(node.getElements());
        }

        final String greatestCommonPrefix = this.getGreatestCommonPrefix(nodePrefix, word);

        if (greatestCommonPrefix.length() < nodePrefix.length()) {
            return new HashSet<>();
        }

        final String wordSuffix = word.substring(greatestCommonPrefix.length());

        if (node.hasChild(wordSuffix.charAt(0))) {
            return this.findRecursively(node.getChild(wordSuffix.charAt(0)),
                    wordSuffix.substring(1));
        }

        return new HashSet<>();
    }

    private Set<E> findRecursivelyByPrefix(final Node<E> node, final String prefix) {
        final String nodePrefix = node.getPrefix();
        final String greatestCommonPrefix = this.getGreatestCommonPrefix(nodePrefix, prefix);

        if (greatestCommonPrefix.length() == prefix.length()) {
            return this.getSubtreeElements(node);
        }

        if (greatestCommonPrefix.length() == nodePrefix.length()) {
            final String remainingPrefix = prefix.substring(greatestCommonPrefix.length());

            if (node.hasChild(remainingPrefix.charAt(0))) {
                return this.findRecursivelyByPrefix(node.getChild(remainingPrefix.charAt(0)),
                        remainingPrefix.substring(1));
            }
        }

        return new HashSet<>();
    }

    private Set<E> getSubtreeElements(final Node<E> subtreeRoot) {
        final Set<E> subtreeElements = new HashSet<>();
        final Iterator<Node<E>> subtreeNodeIterator = new NodeIterator(subtreeRoot);

        while (subtreeNodeIterator.hasNext()) {
            subtreeElements.addAll(subtreeNodeIterator.next().getElements());
        }

        return subtreeElements;
    }

    private void cleanUpRecursively(final Node<E> node) {
        final int numberOfChildNodes = node.getLabels().size();
        final int numberOfReferencedElements = node.getElements().size();

        if (numberOfChildNodes > 1 || numberOfReferencedElements > 0) {
            return;
        }

        if (numberOfChildNodes == 1) {
            final char label = node.getLabels().iterator().next();
            final Node<E> child = node.getChild(label);
            child.setPrefix(node.getPrefix() + label + child.getPrefix());

            if (node == this.root) {
                this.root = child;
            } else {
                final Node<E> parent = node.getParent();
                final char parentEdgeLabel = node.getParentEdgeLabel();

                parent.removeChild(parentEdgeLabel);
                parent.setChild(parentEdgeLabel, child);
            }

            return;
        }

        if (node == this.root) {
            this.root = null;
            return;
        }

        final Node<E> parent = node.getParent();
        final char parentEdgeLabel = node.getParentEdgeLabel();
        parent.removeChild(parentEdgeLabel);

        this.cleanUpRecursively(parent);
    }

    private String getGreatestCommonPrefix(final String word1, final String word2) {
        int commonPrefixLength = 0;

        for (int i = 0; i < Math.min(word1.length(), word2.length()); i++) {
            if (word1.charAt(i) == word2.charAt(i)) {
                commonPrefixLength++;
            } else {
                break;
            }
        }

        return word1.substring(0, commonPrefixLength);
    }

    private void addElementMapping(final E element, final Node<E> node) {
        this.elements.computeIfAbsent(element, k -> new HashSet<>()).add(node);
    }

    // visible for testing
    Node<E> getRoot() {
        return this.root;
    }

    /**
     * Compares the specified object with this prefix tree for equality. Returns true if the object
     * is also a prefix tree and has a root, which is equal to this prefix tree's root.
     *
     * @param o the object to be compared with this prefix tree for equality
     * @return true if the specified object is equal to this prefix tree, false otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final PrefixTree<?> other)) {
            return false;
        }
        return Objects.equals(this.getRoot(), other.getRoot());
    }

    /**
     * Returns the hash code value for this prefix tree, that is the hash code value of its root.
     *
     * @return the hash code value fo this prefix tree
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.getRoot());
    }

    /**
     * Represents a node of a prefix tree. A node has a prefix, outgoing edges to child nodes
     * labeled with characters and outgoing edges to its referenced elements. Additionally, it
     * stores its parent node if present as well as the label on the edge to the parent node.
     *
     * @param <E> the type of the elements referenced by this prefix tree node
     */
    static class Node<E> {

        private final Map<Character, Node<E>> children;
        private final Set<E> elements;
        private String prefix;
        private Node<E> parent;
        private Character parentEdgeLabel;

        /**
         * Creates a new prefix tree node.
         */
        public Node() {
            this.prefix = "";
            this.children = new HashMap<>();
            this.elements = new HashSet<>();
        }

        /**
         * Returns this node's prefix.
         *
         * @return this node's prefix
         */
        public String getPrefix() {
            return this.prefix;
        }

        /**
         * Sets this node's prefix to the specified value.
         * <p>
         * Requires the specified prefix to not be null.
         *
         * @param prefix the value to be set as this node's prefix
         * @throws IllegalArgumentException if the specified prefix is null
         */
        public void setPrefix(final String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("Prefix cannot be null");
            }

            this.prefix = prefix;
        }

        /**
         * Indicates whether this node has a parent node.
         *
         * @return true if a parent node is present, false otherwise
         */
        public boolean hasParent() {
            return this.parent != null;
        }

        /**
         * Returns this node's parent node.
         * <p>
         * Requires this node to have a parent node.
         *
         * @return this node's parent node
         * @throws NoSuchElementException if this node does not have a parent node
         */
        public Node<E> getParent() {
            if (!this.hasParent()) {
                throw new NoSuchElementException("No parent present");
            }

            return this.parent;
        }

        /**
         * Returns the label on the edge to this node's parent node.
         * <p>
         * Requires this node to have a parent node.
         *
         * @return the label on the edge to this node's parent node
         * @throws NoSuchElementException if this node does not have a parent node
         */
        public char getParentEdgeLabel() {
            if (!this.hasParent()) {
                throw new NoSuchElementException("No parent present");
            }
            return this.parentEdgeLabel;
        }

        private void setParent(final Character label, final Node<E> node) {
            this.parent = node;
            this.parentEdgeLabel = label;
        }

        /**
         * Returns a set containing all labels on the outgoing edges of this node.
         *
         * @return the set of labels on this node's outgoing edges
         */
        public Set<Character> getLabels() {
            return this.children.keySet();
        }

        /**
         * Sets the child node for a certain character. Automatically sets this node as the child
         * parent.
         *
         * @param label the label on the edge to the child
         * @param node  the child node
         * @throws IllegalArgumentException if the specified node is null
         */
        public void setChild(final char label, final Node<E> node) {
            if (node == null) {
                throw new IllegalArgumentException("Child node cannot be null");
            }

            this.children.put(label, node);
            node.setParent(label, this);
        }

        /**
         * Indicates whether this node has a child for the specified label.
         *
         * @param label the label to check for
         * @return true if a child is present for the specified label, false otherwise
         */
        public boolean hasChild(final char label) {
            return this.children.containsKey(label);
        }

        /**
         * Returns the child for the specified label.
         * <p>
         * Requires a child to be present for the specified label.
         *
         * @param label the label for which the child is to be returned
         * @throws NoSuchElementException if no child is present for the specified label
         */
        public Node<E> getChild(final char label) {
            if (!this.hasChild(label)) {
                throw new NoSuchElementException("No child present for the specified label");
            }

            return this.children.get(label);
        }

        /**
         * Returns a set containing this node's child nodes.
         *
         * @return a set containing this node's child nodes
         */
        public Set<Node<E>> getChildren() {
            return new HashSet<>(this.children.values());
        }

        /**
         * Removes the child at the specified label if present (optional operation).
         *
         * @param label the label at which the child is to be removed
         */
        public void removeChild(final char label) {
            if (this.children.containsKey(label)) {
                this.children.get(label).setParent(null, null);
                this.children.remove(label);
            }
        }

        /**
         * Adds the provided element to this node's referenced elements if not already present
         * (optional operation).
         *
         * @param element the element to be added
         * @throws IllegalArgumentException if the provided element is null
         */
        public void addElement(final E element) {
            if (element == null) {
                throw new IllegalArgumentException("Cannot add null element");
            }

            this.elements.add(element);
        }

        /**
         * Returns a set containing all elements referenced by this node.
         *
         * @return the set of this node's elements
         */
        public Set<E> getElements() {
            return this.elements;
        }

        /**
         * Removes the provided element from this node's referenced elements if present (optional
         * operation).
         *
         * @param element the element to be removed
         * @throws IllegalArgumentException if the provided element is null
         */
        public void removeElement(final E element) {
            if (element == null) {
                throw new IllegalArgumentException("Cannot remove null element");
            }

            this.elements.remove(element);
        }

        /**
         * Compares the specified object with this node for equality. Returns true if the object is
         * also a prefix tree node and has the same prefix, elements and children as this node.
         *
         * @param o the object to be compared with this node for equality
         * @return true if the specified object is equal to this node, false otherwise
         */
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof final Node<?> other)) {
                return false;
            }
            return Objects.equals(this.getChildren(), other.getChildren())
                    && Objects.equals(this.getElements(), other.getElements())
                    && Objects.equals(this.getPrefix(), other.getPrefix());
        }

        /**
         * Returns the hash code value for this node. The hash code value is defined by this node's
         * prefix, elements and children.
         *
         * @return the hash code value for this node
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.getChildren(), this.getElements(), this.getPrefix());
        }
    }

    private class NodeIterator implements Iterator<Node<E>> {

        private final Deque<Node<E>> deque;

        public NodeIterator(final Node<E> root) {
            this.deque = new ArrayDeque<>();
            if (root != null) {
                this.deque.push(root);
            }
        }

        @Override
        public boolean hasNext() {
            return !this.deque.isEmpty();
        }

        @Override
        public Node<E> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("Next element is not present");
            }

            final Node<E> currentNode = this.deque.pop();

            for (final Node<E> child : currentNode.getChildren()) {
                this.deque.push(child);
            }

            return currentNode;
        }
    }
}
