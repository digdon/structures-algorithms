package main

import (
	"fmt"
	"math/rand"
	"time"
)

type Node struct {
	value       string
	left, right *Node
}

func addToTree(root *Node, value string) *Node {
	if root == nil {
		root = &Node{value: value}
	} else {
		targetNode := root

		for true {
			if value < targetNode.value {
				if targetNode.left == nil {
					targetNode.left = &Node{value: value}
					break
				} else {
					targetNode = targetNode.left
				}
			} else {
				if targetNode.right == nil {
					targetNode.right = &Node{value: value}
					break
				} else {
					targetNode = targetNode.right
				}
			}
		}
	}

	return root
}

func inOrder(node *Node) {
	if node == nil {
		return
	}

	inOrder(node.left)
	println(node.value)
	inOrder(node.right)
}

const charset = "abcdefghijklmnopqrstuvwxyz"

var random = rand.New(rand.NewSource(time.Now().UnixNano()))

func generateValue() string {
	str := make([]byte, random.Intn(10)+5)

	for i := range str {
		str[i] = charset[random.Intn(len(charset))]
	}

	return string(str)
}

func main() {
	var root *Node

	for range 10 {
		root = addToTree(root, generateValue())
	}

	inOrder(root)
	fmt.Println("-----")

	reverse(root)

	inOrder(root)
}

func reverse(node *Node) {
	if node == nil {
		return
	}

	node.left, node.right = node.right, node.left

	reverse(node.left)
	reverse(node.right)
}
