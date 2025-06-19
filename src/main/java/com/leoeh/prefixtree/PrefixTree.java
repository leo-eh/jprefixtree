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
 * Allows insertion of word-value-pairs and enables efficient retrieval of values by word or prefix.
 * Additionally, values can be removed, while keeping the tree structure compact and optimized by
 * removing unused words and merging or deleting obsolete nodes.
 *
 * @param <V> the type of the values stored by this prefix tree
 */
public class PrefixTree<V> {

    private Map<V, Set<Node<V>>> reverseNodeLookupMap;
    private Node<V> root;

    /**
     * Creates a new prefix tree.
     */
    public PrefixTree() {
        this.reverseNodeLookupMap = new HashMap<>();
    }

    /**
     * Returns the number of values stored in this prefix tree.
     *
     * @return the number of values stored in this prefix tree
     */
    public int getSize() {
        return this.reverseNodeLookupMap.keySet().size();
    }

    /**
     * Returns the number of nodes present in this prefix tree.
     *
     * @return the number of nodes present in this prefix tree
     */
    public int getNodeCount() {
        final Iterator<Node<V>> nodeIterator = new NodeIterator(this.root);
        int nodeCount = 0;

        while (nodeIterator.hasNext()) {
            nodeIterator.next();
            nodeCount++;
        }

        return nodeCount;
    }

    /**
     * Inserts the specified word into the prefix tree and associates the given value with the word,
     * if it is not already associated with that word (optional operation). The value can later be
     * retrieved by searching for the word.
     * <p>
     * Requires the word to be at least one character long.
     *
     * @param word  the word used to find the value
     * @param value the value to be associated with the word
     * @throws IllegalArgumentException if the specified word is null or empty or if the provided
     *                                  value is null
     */
    public void insert(final String word, final V value) {
        if (word == null) {
            throw new IllegalArgumentException("Word cannot be null");
        }
        if (word.isEmpty()) {
            throw new IllegalArgumentException("Word cannot be empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        final String lowerCaseWord = word.toLowerCase();

        if (this.root == null) {
            this.root = new Node<>();
            this.root.setPrefix(lowerCaseWord);
            this.root.addValue(value);
            this.addReverseNodeMapping(value, this.root);
        } else {
            this.insertRecursively(this.root, lowerCaseWord, value);
        }
    }

    /**
     * Searches for the specified word in this prefix tree and returns a set containing all values
     * associated with that word.
     *
     * @param word the word whose associated values are to be found
     * @return the set of values associated with the specified word
     * @throws IllegalArgumentException if the specified word is null or empty
     */
    public Set<V> find(final String word) {
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
     * all values associated with those words.
     *
     * @param prefix the prefix of the words whose associated values are to be found
     * @return the set of values associated with words beginning with the specified prefix
     * @throws IllegalArgumentException if the specified prefix is null or empty
     */
    public Set<V> findByPrefix(final String prefix) {
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
     * Removes the specified value from this prefix tree, if present (optional operation). Also
     * removes all words which point exclusively to this value and optimizes the tree structure by
     * merging nodes and removing unnecessary nodes if possible.
     *
     * @param value the value to be removed
     * @throws IllegalArgumentException if the provided value is null
     */
    public void remove(final V value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        if (!this.reverseNodeLookupMap.containsKey(value)) {
            return;
        }

        final Set<Node<V>> nodesReferencingValue = this.reverseNodeLookupMap.get(value);
        for (final Node<V> node : nodesReferencingValue) {
            node.removeValue(value);
            this.cleanUpRecursively(node);
        }

        this.reverseNodeLookupMap.remove(value);
    }

    /**
     * Removes all entries from this prefix tree. Ensures that this tree is empty.
     */
    public void clear() {
        this.reverseNodeLookupMap = new HashMap<>();
        this.root = null;
    }

    private void insertRecursively(final Node<V> node, final String word, final V value) {
        final String nodePrefix = node.getPrefix();

        if (nodePrefix.equals(word)) {
            node.addValue(value);
            this.addReverseNodeMapping(value, node);
            return;
        }

        final String greatestCommonPrefix = this.getGreatestCommonPrefix(nodePrefix, word);
        final String nodeSuffix = nodePrefix.substring(greatestCommonPrefix.length());
        final String wordSuffix = word.substring(greatestCommonPrefix.length());

        if (greatestCommonPrefix.length() < nodePrefix.length()) {
            final Node<V> newNode1 = new Node<>();

            newNode1.setPrefix(greatestCommonPrefix);
            node.setPrefix(nodeSuffix.substring(1));

            if (node == this.root) {
                this.root = newNode1;
            } else {
                final Node<V> parent = node.getParent();
                final char parentEdgeLabel = node.getParentEdgeLabel();

                parent.setChild(parentEdgeLabel, newNode1);
            }

            newNode1.setChild(nodeSuffix.charAt(0), node);

            if (!wordSuffix.isEmpty()) {
                final Node<V> newNode2 = new Node<>();

                newNode2.setPrefix(wordSuffix.substring(1));
                newNode2.addValue(value);
                this.addReverseNodeMapping(value, newNode2);

                newNode1.setChild(wordSuffix.charAt(0), newNode2);
            } else {
                newNode1.addValue(value);
                this.addReverseNodeMapping(value, newNode1);
            }

            return;
        }

        if (node.hasChild(wordSuffix.charAt(0))) {
            this.insertRecursively(node.getChild(wordSuffix.charAt(0)),
                    wordSuffix.substring(1),
                    value);
            return;
        }

        final Node<V> newNode2 = new Node<>();

        newNode2.setPrefix(wordSuffix.substring(1));
        newNode2.addValue(value);
        this.addReverseNodeMapping(value, newNode2);

        node.setChild(wordSuffix.charAt(0), newNode2);
    }

    private Set<V> findRecursively(final Node<V> node, final String word) {
        final String nodePrefix = node.getPrefix();

        if (nodePrefix.equals(word)) {
            return new HashSet<>(node.getValues());
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

    private Set<V> findRecursivelyByPrefix(final Node<V> node, final String prefix) {
        final String nodePrefix = node.getPrefix();
        final String greatestCommonPrefix = this.getGreatestCommonPrefix(nodePrefix, prefix);

        if (greatestCommonPrefix.length() == prefix.length()) {
            return this.getSubtreeValues(node);
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

    private Set<V> getSubtreeValues(final Node<V> subtreeRoot) {
        final Set<V> subtreeValues = new HashSet<>();
        final Iterator<Node<V>> subtreeNodeIterator = new NodeIterator(subtreeRoot);

        while (subtreeNodeIterator.hasNext()) {
            subtreeValues.addAll(subtreeNodeIterator.next().getValues());
        }

        return subtreeValues;
    }

    private void cleanUpRecursively(final Node<V> node) {
        final int numberOfChildNodes = node.getLabels().size();
        final int numberOfReferencedValues = node.getValues().size();

        if (numberOfChildNodes > 1 || numberOfReferencedValues > 0) {
            return;
        }

        if (numberOfChildNodes == 1) {
            final char label = node.getLabels().iterator().next();
            final Node<V> child = node.getChild(label);
            child.setPrefix(node.getPrefix() + label + child.getPrefix());

            if (node == this.root) {
                this.root = child;
            } else {
                final Node<V> parent = node.getParent();
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

        final Node<V> parent = node.getParent();
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

    private void addReverseNodeMapping(final V value, final Node<V> node) {
        this.reverseNodeLookupMap.computeIfAbsent(value, k -> new HashSet<>()).add(node);
    }

    // visible for testing
    Node<V> getRoot() {
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
     * labeled with characters and outgoing edges to its stored values. Additionally, it stores its
     * parent node if present as well as the label on the edge to the parent node.
     *
     * @param <V> the type of the values stored in this prefix tree node
     */
    static class Node<V> {

        private final Map<Character, Node<V>> children;
        private final Set<V> values;
        private String prefix;
        private Node<V> parent;
        private Character parentEdgeLabel;

        /**
         * Creates a new prefix tree node.
         */
        public Node() {
            this.prefix = "";
            this.children = new HashMap<>();
            this.values = new HashSet<>();
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
        public Node<V> getParent() {
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

        private void setParent(final Character label, final Node<V> node) {
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
        public void setChild(final char label, final Node<V> node) {
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
        public Node<V> getChild(final char label) {
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
        public Set<Node<V>> getChildren() {
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
         * Adds the provided value to the set of values stored in this node if not already present
         * (optional operation).
         *
         * @param value the value to be added
         * @throws IllegalArgumentException if the provided value is null
         */
        public void addValue(final V value) {
            if (value == null) {
                throw new IllegalArgumentException("Cannot add null value");
            }

            this.values.add(value);
        }

        /**
         * Returns a set containing all values stored in this node.
         *
         * @return the set of values stored in this node
         */
        public Set<V> getValues() {
            return this.values;
        }

        /**
         * Removes the provided value from the set of values stored in this node if present
         * (optional operation).
         *
         * @param value the value to be removed
         * @throws IllegalArgumentException if the provided value is null
         */
        public void removeValue(final V value) {
            if (value == null) {
                throw new IllegalArgumentException("Cannot remove null value");
            }

            this.values.remove(value);
        }

        /**
         * Compares the specified object with this node for equality. Returns true if the object is
         * also a prefix tree node and has the same prefix, stored values and children as this
         * node.
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
                    && Objects.equals(this.getValues(), other.getValues())
                    && Objects.equals(this.getPrefix(), other.getPrefix());
        }

        /**
         * Returns the hash code value for this node. The hash code value is defined by this node's
         * prefix, stored values and children.
         *
         * @return the hash code value for this node
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.getChildren(), this.getValues(), this.getPrefix());
        }
    }

    private class NodeIterator implements Iterator<Node<V>> {

        private final Deque<Node<V>> deque;

        public NodeIterator(final Node<V> root) {
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
        public Node<V> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("Next element is not present");
            }

            final Node<V> currentNode = this.deque.pop();

            for (final Node<V> child : currentNode.getChildren()) {
                this.deque.push(child);
            }

            return currentNode;
        }
    }
}
