# JPrefixTree
[![JitPack](https://jitpack.io/v/leo-eh/jprefixtree.svg)](https://jitpack.io/#leo-eh/jprefixtree)
[![Build](https://github.com/leo-eh/jprefixtree/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/leo-eh/jprefixtree/actions/workflows/build.yml)

A prefix tree implementation for Java.

## Installation
### Using Maven:
First, add the JitPack repository to your `pom.xml`:
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```
Then include the dependency:
```xml
<dependencies>
  <dependency>
    <groupId>com.github.leo-eh</groupId>
    <artifactId>jprefixtree</artifactId>
    <version>0.1.2</version>
  </dependency>
</dependencies>
```

## API Documentation
The latest API documentation can be found [here](https://jitpack.io/com/github/leo-eh/jprefixtree/latest/javadoc/).

## Example Usage
In this example, a prefix tree is used to search for books by a word from their title.
For this the books' titles are split into individual words. These words are then added to the prefix
tree and associated with their corresponding book, using `insert`.

```java
import com.leoeh.prefixtree.PrefixTree;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        PrefixTree<Book> prefixTree = new PrefixTree<>();

        Book bookA = new Book("Efficient Search Algorithms with Prefix Trees");
        Book bookB = new Book("Mastering Tries and Prefix Trees");

        for (String word : List.of(bookA.title.split(" "))) {
            prefixTree.insert(word, bookA);
        }

        for (String word : List.of(bookB.title.split(" "))) {
            prefixTree.insert(word, bookB);
        }

        System.out.format("The following book(s) have the word \"efficient\" in their titles: %s%n",
                prefixTree.find("efficient").toString());

        System.out.format("The following book(s) have the word \"prefix\" in their titles: %s%n",
                prefixTree.find("prefix").toString());

        prefixTree.remove(bookA);

        System.out.format("After deleting bookA, nothing is found for \"algorithms\": %s%n ",
                prefixTree.find("algorithms").toString());
    }

    public static class Book {

        private final String title;

        public Book(String title) {
            this.title = title;
        }

        public String getTitle() {
            return this.title;
        }

        @Override
        public String toString() {
            return this.getTitle();
        }
    }
}
```

## Implementation

This prefix tree implementation uses nodes, which store a (possibly empty) prefix, along with their
child nodes, the characters leading to those children, and the associated elements. Nodes are only
split into smaller nodes (by prefix length) when necessary.

The following example tree illustrates the prefix tree's structure.
It shows the tree, which results from executing the following commands (order irrelevant):
```java
prefixTree.insert("app", element1);
prefixTree.insert("app", element3);
prefixTree.insert("apple", element2);
prefixTree.insert("banana", element4);
prefixTree.insert("ball", element4);
prefixTree.insert("bat", element5);
prefixTree.insert("batman", element1);
```

![example-tree-01-dark](https://github.com/user-attachments/assets/5d9f2b2d-2245-4b99-b7e8-b27b6c6384c2)
----------------------------------------------------------------------------------------------------
When removing elements from the prefix tree, the structure is automatically optimized by removing
unused Nodes and merging single child nodes with their parent.
This way the tree always remains compact.






