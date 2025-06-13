package com.leoeh.prefixtree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        void testErrorCase_elementIsNull() {
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
        void testNormalCase_multipleElementsUsingOneWord() {
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
            tree.insert("Tree", 4);

            tree.insert("Treehouse", 2);

            assertEquals(1, tree.find("Tree").size());
            assertTrue(tree.find("Tree").contains(4));
            assertEquals(1, tree.find("Treehouse").size());
            assertTrue(tree.find("Treehouse").contains(2));
        }

        @Test
        void testEdgeCase_newWordIsPrefixOfPresentWord() {
            tree.insert("Treehouse", 2);

            tree.insert("Tree", 4);

            assertEquals(1, tree.find("Tree").size());
            assertTrue(tree.find("Tree").contains(4));
            assertEquals(1, tree.find("Treehouse").size());
            assertTrue(tree.find("Treehouse").contains(2));
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
    @DisplayName("remove")
    class Remove {

        @Test
        void testErrorCase_elementIsNull() {
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
        void testEdgeCase_treeDoesNotContainElement() {
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
            assertEquals(1, tree.getNodeCount());

            tree.remove(1);
            assertEquals(0, tree.getNodeCount());
        }

        @Test
        void testNormalCase_multipleWords() {
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

            assertEquals(5, tree.getNodeCount());

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
    }

    @Nested
    @DisplayName("getSize")
    class GetSize {

        @Test
        void testNormalCase_treeIsEmpty() {
            assertEquals(0, tree.getSize());
        }

        @Test
        void testNormalCase_multipleDistinctElementsInserted() {
            tree.insert("Test", 1);
            tree.insert("Prefix", 3);
            tree.insert("Tree", 19);
            tree.insert("Patricia", 8);

            assertEquals(4, tree.getSize());
        }

        @Test
        void testEdgeCase_equalElementsInserted() {
            tree.insert("Test", 1);
            tree.insert("Tree", 1);
            tree.insert("Prefix", 4);
            tree.insert("Prefix", 1);

            assertEquals(2, tree.getSize());
        }

        @Test
        void testEdgeCase_elementsInsertedAndRemoved() {
            tree.insert("Test", 1);
            tree.insert("Prefix", 3);
            tree.insert("Tree", 19);
            tree.insert("Patricia", 8);

            tree.remove(3);
            tree.remove(8);

            assertEquals(2, tree.getSize());
        }
    }
}