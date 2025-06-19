package com.leoeh.prefixtree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leoeh.prefixtree.PrefixTree.Node;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PrefixTreeTest {

    private PrefixTree<Integer> tree;

    @BeforeEach
    void setUp() {
        this.tree = new PrefixTree<>();
    }

    @Nested
    @DisplayName("insert")
    class Insert {

        @Test
        void testErrorCase_wordIsNull() {
            assertThrows(IllegalArgumentException.class, () -> tree.insert(null, 1));
        }

        @Test
        void testErrorCase_wordIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> tree.insert("", 1));
        }

        @Test
        void testErrorCase_valueIsNull() {
            assertThrows(IllegalArgumentException.class, () -> tree.insert("Hello World", null));
        }

        @Test
        void testNormalCase() {
            final Set<Integer> expected = Set.of(1);

            tree.insert("test", 1);
            final Set<Integer> actual = tree.find("test");

            assertEquals(expected, actual);
        }

        @Test
        void testEdgeCase_wordIsNotLowerCase() {
            final Set<Integer> expected = Set.of(2);

            tree.insert("Test", 2);
            final Set<Integer> actual = tree.find("Test");

            assertEquals(expected, actual);
        }

        @Test
        void testNormalCase_multipleValuesUsingOneWord() {
            final Set<Integer> expected = Set.of(2, 5, 4, 8, 9, 0);
            tree.insert("Tree", 1);
            tree.insert("Prefix", 7);

            tree.insert("Test", 2);
            tree.insert("Test", 5);
            tree.insert("test", 4);
            tree.insert("test", 8);
            tree.insert("test", 9);
            tree.insert("test", 0);
            final Set<Integer> actual = tree.find("Test");

            assertEquals(expected, actual);
        }

        @Test
        void testEdgeCase_prefixOfNewWordAlreadyPresent() {
            tree.insert("Tree", 2);

            tree.insert("Treehouse", 1);

            /*
             * tree(4)
             *      |---> 2
             *      |
             *      |--h--ouse(0)
             *             |---> 1
             *
             */
            assertEquals(2, tree.getNodeCount());

            final Node<Integer> root = tree.getRoot();
            assertEquals("tree", root.getPrefix());
            assertEquals(Set.of(2), root.getValues());
            assertEquals(1, root.getChildren().size());
            assertTrue(root.hasChild('h'));

            final Node<Integer> child = root.getChild('h');
            assertEquals("ouse", child.getPrefix());
            assertEquals(Set.of(1), child.getValues());
            assertTrue(child.getChildren().isEmpty());
        }

        @Test
        void testEdgeCase_newWordIsPrefixOfPresentWord() {
            tree.insert("Treehouse", 1);

            tree.insert("Tree", 2);

            /*
             * tree(4)
             *      |---> 2
             *      |
             *      |--h--ouse(0)
             *             |---> 1
             *
             */
            assertEquals(2, tree.getNodeCount());

            final Node<Integer> root = tree.getRoot();
            assertEquals("tree", root.getPrefix());
            assertEquals(Set.of(2), root.getValues());
            assertEquals(1, root.getChildren().size());
            assertTrue(root.hasChild('h'));

            final Node<Integer> child = root.getChild('h');
            assertEquals("ouse", child.getPrefix());
            assertEquals(Set.of(1), child.getValues());
            assertTrue(child.getChildren().isEmpty());
        }
    }

    @Nested
    @DisplayName("find")
    class Find {

        @Test
        void testErrorCase_wordIsNull() {
            assertThrows(IllegalArgumentException.class, () -> tree.find(null));
        }

        @Test
        void testErrorCase_wordIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> tree.find(""));
        }

        @Test
        void testEdgeCase_treeIsEmpty() {
            assertTrue(tree.find("Test").isEmpty());
        }

        @Test
        void testEdgeCase_treeContainsPrefixOfWord() {
            tree.insert("Tree", 1);
            tree.insert("Prefix", 2);

            assertTrue(tree.find("Treehouse").isEmpty());
        }
    }

    @Nested
    @DisplayName("findByPrefix")
    class FindByPrefix {

        @Test
        void testNormalCase() {
            tree.insert("tree", 1);
            tree.insert("treehouse", 2);

            assertEquals(Set.of(1, 2), tree.findByPrefix("tree"));
        }

        @Test
        void testNormalCase_longPrefix() {
            tree.insert("trie", 0);
            tree.insert("tree", 1);
            tree.insert("treehouse", 2);
            tree.insert("treehousewindow", 3);

            assertEquals(Set.of(2, 3), tree.findByPrefix("treeh"));
        }

        @Test
        void testEdgeCase_treeIsEmpty() {
            assertTrue(tree.findByPrefix("test").isEmpty());
        }

        @Test
        void testEdgeCase_noMatchingWordsPresent() {
            tree.insert("tree", 1);
            tree.insert("treehouse", 2);
            tree.insert("prefix", 3);

            assertTrue(tree.findByPrefix("a").isEmpty());
        }

        @Test
        void testErrorCase_prefixIsNull() {
            assertThrows(IllegalArgumentException.class, () -> tree.findByPrefix(null));
        }

        @Test
        void testErrorCase_prefixIsEmpty() {
            assertThrows(IllegalArgumentException.class, () -> tree.findByPrefix(""));
        }
    }

    @Nested
    @DisplayName("remove")
    class Remove {

        @Test
        void testErrorCase_valueIsNull() {
            assertThrows(IllegalArgumentException.class, () -> tree.remove(null));
        }

        @Test
        void testNormalCase() {
            tree.insert("Tree", 1);
            tree.insert("Prefix", 7);
            tree.insert("Prefix", 6);
            tree.insert("Patricia", 7);
            assertEquals(1, tree.find("Tree").size());
            assertEquals(2, tree.find("Prefix").size());
            assertEquals(1, tree.find("Patricia").size());

            tree.remove(7);

            final Set<Integer> expected = Set.of(6);
            final Set<Integer> actual = tree.find("Prefix");

            assertEquals(expected, actual);
            assertEquals(0, tree.find("Patricia").size());
        }

        @Test
        void testEdgeCase_treeDoesNotContainValue() {
            tree.insert("Tree", 1);
            tree.insert("Prefix", 7);
            tree.insert("Prefix", 6);
            tree.insert("Patricia", 7);

            tree.remove(2);

            assertEquals(1, tree.find("Tree").size());
            assertEquals(2, tree.find("Prefix").size());
            assertEquals(1, tree.find("Patricia").size());
        }
    }

    @Nested
    @DisplayName("remove (cleanUp)")
    class RemoveCleanUp {

        @Test
        void testNormalCase_oneWord() {
            tree.insert("Tree", 1);

            tree.remove(1);

            assertEquals(0, tree.getNodeCount());
        }

        @Test
        void testNormalCase_noCleanUpRequired() {
            tree.insert("Tree", 1);
            tree.insert("Tree", 2);
            tree.insert("Prefix", 3);
            tree.insert("Prefix", 2);
            tree.insert("Patricia", 5);

            /*
             * (0)
             *  |--t--ree(3)
             *  |         |---> 1
             *  |         |---> 2
             *  |
             *  |--p--(0)
             *         |--r--efix(4)
             *         |          |---> 3
             *         |          |---> 2
             *         |
             *         |--a--tricia(6)
             *                      |---> 5
             *
             */

            tree.remove(2);

            /*
             * (0)
             *  |--t--ree(3)
             *  |         |---> 1
             *  |
             *  |
             *  |--p--(0)
             *         |--r--efix(4)
             *         |          |---> 3
             *         |
             *         |
             *         |--a--tricia(6)
             *                      |---> 5
             *
             */
            assertEquals(5, tree.getNodeCount());

            final Node<Integer> root = tree.getRoot();
            assertEquals("", root.getPrefix());
            assertTrue(root.getValues().isEmpty());
            assertEquals(Set.of('t', 'p'), root.getLabels());

            final Node<Integer> child1 = root.getChild('t');
            assertEquals("ree", child1.getPrefix());
            assertEquals(Set.of(1), child1.getValues());
            assertTrue(child1.getChildren().isEmpty());

            final Node<Integer> child2 = root.getChild('p');
            assertEquals("", child2.getPrefix());
            assertTrue(child2.getValues().isEmpty());
            assertEquals(Set.of('r', 'a'), child2.getLabels());

            final Node<Integer> child21 = child2.getChild('r');
            assertEquals("efix", child21.getPrefix());
            assertEquals(Set.of(3), child21.getValues());
            assertTrue(child21.getChildren().isEmpty());

            final Node<Integer> child22 = child2.getChild('a');
            assertEquals("tricia", child22.getPrefix());
            assertEquals(Set.of(5), child22.getValues());
            assertTrue(child22.getChildren().isEmpty());

            tree.remove(3);

            /*
             * (0)
             *  |--t--ree(3)
             *  |         |---> 1
             *  |
             *  |
             *  |--p--atricia(7)
             *                |---> 5
             *
             */

            assertEquals(3, tree.getNodeCount());

            tree.remove(1);

            /*
             * patricia(8)
             *          |---> 5
             *
             */

            assertEquals(1, tree.getNodeCount());

            tree.remove(5);

            assertEquals(0, tree.getNodeCount());
        }

        @Test
        void testEdgeCase_nodeHasNoValuesAfterRemoval() {
            tree.insert("tree", 1);
            tree.insert("prefix", 3);
            tree.insert("patricia", 5);

            /*
             * (0)
             *  |--t--ree(3)
             *  |         |---> 1
             *  |
             *  |
             *  |--p--(0)
             *         |--r--efix(4)
             *         |          |---> 3
             *         |
             *         |
             *         |--a--tricia(6)
             *                      |---> 5
             *
             */

            tree.remove(3);

            /*
             * (0)
             *  |--t--ree(3)
             *  |         |---> 1
             *  |
             *  |
             *  |--p--atricia(7)
             *                |---> 5
             *
             */
            assertEquals(3, tree.getNodeCount());

            final Node<Integer> root = tree.getRoot();
            assertEquals("", root.getPrefix());
            assertTrue(root.getValues().isEmpty());
            assertEquals(Set.of('t', 'p'), root.getLabels());

            final Node<Integer> child1 = root.getChild('t');
            assertEquals("ree", child1.getPrefix());
            assertEquals(Set.of(1), child1.getValues());
            assertTrue(child1.getChildren().isEmpty());

            final Node<Integer> child2 = root.getChild('p');
            assertEquals("atricia", child2.getPrefix());
            assertEquals(Set.of(5), child2.getValues());
            assertTrue(child2.getChildren().isEmpty());
        }

        @Test
        void testEdgeCase_wordAndItsPrefix() {
            tree.insert("Hello", 1);
            tree.insert("Hell", 2);

            /*
             * hell(4)
             *      |---> 2
             *      |
             *      |--o--(0)
             *             |---> 1
             *
             */

            tree.remove(2);

            /*
             * hello(5)
             *       |---> 1
             *
             */
            assertEquals(1, tree.getNodeCount());

            final Node<Integer> root = tree.getRoot();
            assertEquals("hello", root.getPrefix());
            assertEquals(Set.of(1), root.getValues());
            assertTrue(root.getChildren().isEmpty());

            tree.remove(1);

            assertEquals(0, tree.getNodeCount());
        }
    }

    @Nested
    @DisplayName("clear")
    class Clear {

        @Test
        void testNormalCase() {
            tree.insert("Test", 1);
            tree.insert("Prefix", 3);
            tree.insert("Tree", 19);
            tree.insert("Patricia", 8);

            tree.clear();

            assertEquals(tree, new PrefixTree<Integer>());
            assertEquals(0, tree.getNodeCount());
            assertEquals(0, tree.getSize());
        }

        @Test
        void testEdgeCase_treeIsEmpty() {
            tree.clear();

            assertEquals(tree, new PrefixTree<Integer>());
            assertEquals(0, tree.getNodeCount());
            assertEquals(0, tree.getSize());
        }
    }

    @Nested
    @DisplayName("getSize")
    class GetSize {

        @Test
        void testNormalCase_treeIsEmpty() {
            assertEquals(0, tree.getSize());
        }

        @Test
        void testNormalCase_multipleDistinctValuesInserted() {
            tree.insert("Test", 1);
            tree.insert("Prefix", 3);
            tree.insert("Tree", 19);
            tree.insert("Patricia", 8);

            assertEquals(4, tree.getSize());
        }

        @Test
        void testEdgeCase_equalValuesInserted() {
            tree.insert("Test", 1);
            tree.insert("Tree", 1);
            tree.insert("Prefix", 4);
            tree.insert("Prefix", 1);

            assertEquals(2, tree.getSize());
        }

        @Test
        void testEdgeCase_valuesInsertedAndRemoved() {
            tree.insert("Test", 1);
            tree.insert("Prefix", 3);
            tree.insert("Tree", 19);
            tree.insert("Patricia", 8);

            tree.remove(3);
            tree.remove(8);

            assertEquals(2, tree.getSize());
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        void testNormalCase() {
            final PrefixTree<Integer> otherTree = new PrefixTree<>();
            List.of(tree, otherTree).forEach(prefixTree -> {
                prefixTree.insert("Tree", 1);
                prefixTree.insert("Treehouse", 2);
                prefixTree.insert("Treehouse", 3);
                prefixTree.insert("Trie", 7);
                prefixTree.insert("House", 1);
            });

            assertEquals(tree, otherTree);
            assertEquals(otherTree, tree);

            assertEquals(tree.hashCode(), otherTree.hashCode());
        }

        @Test
        void testEdgeCase_bothTreesEmpty() {
            final PrefixTree<Integer> otherTree = new PrefixTree<>();

            assertEquals(tree, otherTree);
            assertEquals(otherTree, tree);

            assertEquals(tree.hashCode(), otherTree.hashCode());
        }

        @Test
        void testEdgeCase_oneTreeEmpty() {
            final PrefixTree<Integer> otherTree = new PrefixTree<>();
            tree.insert("Test", 3);

            assertNotEquals(tree, otherTree);
            assertNotEquals(otherTree, tree);
        }

        @Test
        void testEdgeCase_afterDeletions() {
            final PrefixTree<Integer> otherTree = new PrefixTree<>();
            List.of(tree, otherTree).forEach(prefixTree -> {
                prefixTree.insert("Tree", 1);
                prefixTree.insert("Treehouse", 2);
                prefixTree.insert("Treehouse", 3);
                prefixTree.insert("Trie", 7);
                prefixTree.insert("House", 1);
            });

            otherTree.insert("Test", 5);
            otherTree.insert("Hash", 6);
            otherTree.remove(5);
            otherTree.remove(6);

            assertEquals(tree, otherTree);
            assertEquals(otherTree, tree);

            assertEquals(tree.hashCode(), otherTree.hashCode());
        }
    }
}