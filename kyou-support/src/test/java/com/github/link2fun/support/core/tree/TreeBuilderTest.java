package com.github.link2fun.support.core.tree;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TreeBuilder 的纯逻辑测试: 顶点判定、逐层挂接、孤儿/环/空列表语义。
 */
public class TreeBuilderTest {

  /** 测试节点: id / parentId / children */
  @Getter
  @Setter
  private static final class Node {
    private final Long id;
    private final Long parentId;
    private List<Node> children = new ArrayList<>();

    Node(final Long id, final Long parentId) {
      this.id = id;
      this.parentId = parentId;
    }
  }

  @SafeVarargs
  private static List<Node> flat(final Node... nodes) {
    return new ArrayList<>(List.of(nodes));
  }

  @Test
  @DisplayName("多层节点逐层挂接, 只返回顶级节点")
  void buildsHierarchy() {
    final Node root = new Node(1L, 0L);
    final Node child = new Node(2L, 1L);
    final Node grand = new Node(3L, 2L);

    final List<Node> tree = TreeBuilder.build(flat(root, child, grand),
      Node::getId, Node::getParentId, Node::setChildren);

    assertEquals(List.of(root), tree);
    assertEquals(List.of(child), root.children);
    assertEquals(List.of(grand), child.children);
  }

  @Test
  @DisplayName("两棵树并存时各自独立挂接")
  void buildsForest() {
    final Node rootA = new Node(1L, 0L);
    final Node childA = new Node(2L, 1L);
    final Node rootB = new Node(10L, 0L);
    final Node childB = new Node(11L, 10L);

    final List<Node> tree = TreeBuilder.build(flat(rootA, rootB, childA, childB),
      Node::getId, Node::getParentId, Node::setChildren);

    assertEquals(2, tree.size());
    assertEquals(List.of(childA), rootA.children);
    assertEquals(List.of(childB), rootB.children);
  }

  @Test
  @DisplayName("父节点不在集合中的孤儿节点视为顶级")
  void orphanParentTreatedAsRoot() {
    final Node root = new Node(1L, 0L);
    final Node orphan = new Node(9L, 999L);

    final List<Node> tree = TreeBuilder.build(flat(root, orphan),
      Node::getId, Node::getParentId, Node::setChildren);

    assertEquals(2, tree.size(), "孤儿节点与正常顶级节点并列返回");
  }

  @Test
  @DisplayName("parentId 为 null 的节点视为顶级")
  void nullParentTreatedAsRoot() {
    final Node root = new Node(1L, null);
    final Node child = new Node(2L, 1L);

    final List<Node> tree = TreeBuilder.build(flat(root, child),
      Node::getId, Node::getParentId, Node::setChildren);

    assertEquals(List.of(root), tree);
    assertEquals(List.of(child), root.children);
  }

  @Test
  @DisplayName("互为父子成环时组不出树, 原样返回兜底")
  void cycleFallsBackToOriginalList() {
    final Node a = new Node(1L, 2L);
    final Node b = new Node(2L, 1L);
    final List<Node> nodes = flat(a, b);

    final List<Node> tree = TreeBuilder.build(nodes, Node::getId, Node::getParentId, Node::setChildren);

    assertEquals(nodes, tree, "环上节点不递归, 返回原列表");
  }

  @Test
  @DisplayName("叶子节点也写回空 children 列表")
  void leafGetsEmptyChildren() {
    final Node root = new Node(1L, 0L);
    final Node leaf = new Node(2L, 1L);

    TreeBuilder.build(flat(root, leaf), Node::getId, Node::getParentId, Node::setChildren);

    assertEquals(List.of(leaf), root.children);
    assertTrue(leaf.children.isEmpty(), "叶子节点的 children 为空列表而非 null");
  }

  @Test
  @DisplayName("空列表返回空列表")
  void emptyListReturnsEmpty() {
    assertTrue(TreeBuilder.build(List.of(), Node::getId, Node::getParentId, Node::setChildren).isEmpty());
  }

  @Test
  @DisplayName("buildByRootParent 只从等值根向下挂接, 孤儿节点不升级为顶级")
  void buildByRootParentDropsOrphans() {
    final Node root = new Node(1L, 0L);
    final Node child = new Node(2L, 1L);
    final Node orphan = new Node(9L, 999L);

    final List<Node> tree = TreeBuilder.buildByRootParent(flat(root, child, orphan), 0L,
      Node::getId, Node::getParentId, Node::setChildren);

    assertEquals(List.of(root), tree, "孤儿节点不出现, 与路由树语义一致");
    assertEquals(List.of(child), root.children);
  }

  @Test
  @DisplayName("buildByRootParent 无等值根时返回空列表")
  void buildByRootParentNoRootReturnsEmpty() {
    final Node midLevel = new Node(5L, 1L);

    final List<Node> tree = TreeBuilder.buildByRootParent(flat(midLevel), 0L,
      Node::getId, Node::getParentId, Node::setChildren);

    assertTrue(tree.isEmpty());
  }

  /** 字符串 id 测试节点 */
  @Getter
  @Setter
  private static final class StringNode {
    private final String id;
    private final String parentId;
    private List<StringNode> children = new ArrayList<>();

    StringNode(final String id, final String parentId) {
      this.id = id;
      this.parentId = parentId;
    }
  }

  @Test
  @DisplayName("id 类型泛型: String id 同样组树")
  void stringIdBuildsTree() {
    final StringNode root = new StringNode("a", null);
    final StringNode child = new StringNode("b", "a");
    final StringNode grand = new StringNode("c", "b");

    final List<StringNode> tree = TreeBuilder.build(new ArrayList<>(List.of(grand, root, child)),
      StringNode::getId, StringNode::getParentId, StringNode::setChildren);

    assertEquals(List.of(root), tree);
    assertEquals(List.of(child), root.children);
    assertEquals(List.of(grand), child.children);
  }
}
