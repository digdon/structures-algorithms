package main

import (
	"cmp"
	"fmt"
	"math/rand"
)

func main() {
	letters := []string{
		"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
		"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
	}

	list := New[string]()
	for len(letters) > 0 {
		index := rand.Intn(len(letters))
		letter := letters[index]
		letters = append(letters[:index], letters[index+1:]...)
		list.Add(letter)
	}

	list.Display()

	fmt.Println(list.Search("d"))
	fmt.Println(list.Search("1"))

	// list.Delete("d")
	// list.Display()
	for item := list.levels; item != nil; item = item.next {
		if item.value != nil {
			fmt.Println("Deleting item", *item.value)
			list.Delete(*item.value)
			list.Display()
		}
	}
}

type SkipList[T any] struct {
	compare func(T, T) int
	levels  *node[T]
}

type node[T any] struct {
	value      *T
	prev, next *node[T]
	up, down   *node[T]
}

// Function to create a new skip list for ordered types
func New[T cmp.Ordered]() *SkipList[T] {
	return &SkipList[T]{compare: cmp.Compare[T]}
}

// Function to create a new skip list with a custom comparator
func NewWithComparator[T any](compare func(T, T) int) *SkipList[T] {
	if compare == nil {
		panic("skip list comparator cannot be nil")
	}

	return &SkipList[T]{compare: compare}
}

// Inserts value into the skip list while maintaining sorted order. "Flips a coin" to decide
// whether to insert the node into the next higher level (creating that level if necessary)
// Duplicate values are allowed.
func (list *SkipList[T]) Add(value T) {
	if list.levels == nil {
		list.levels = &node[T]{}
		item := &node[T]{value: &value}
		item.prev = list.levels
		list.levels.next = item
		return
	}

	// Search for the correct insertion position starting from the top level
	pos := list.levels

	for level := list.levels; level != nil; {
		for node := level; node != nil; node = node.next {
			pos = node

			if node.next != nil && list.compare(*node.next.value, value) > 0 {
				// Next node is greater than the value, so we're done with this level
				break
			}
		}

		level = pos.down
	}

	// We're now at the correct position in the bottom level. From here, we insert the new node,
	// then "coin flip" to see if we should add the node to any higher levels.
	var lowerLevelPos *node[T]
	levelCreated := false

	for {
		// Insert the node at the correct position in the current level
		newItem := &node[T]{value: &value}
		newItem.prev = pos
		newItem.next = pos.next
		if pos.next != nil {
			pos.next.prev = newItem
		}
		pos.next = newItem

		if lowerLevelPos != nil {
			newItem.down = lowerLevelPos
			lowerLevelPos.up = newItem
		}

		lowerLevelPos = newItem

		if levelCreated {
			// We created a new level in the previous iteration, so we'll stop here so we don't create
			// multiple new levels for a single item
			break
		}

		// Coin flip
		if !coinFlip() {
			// Flip failed, so we're done
			break
		}

		// Now we're going to insert into the level above. To do this, we first need to go backwards
		// from the current node until we find one that points to the level above
		for pos.up == nil {
			if pos.prev == nil {
				break
			}

			pos = pos.prev
		}

		if pos.up == nil {
			// We're at the top level, so we need to create a new level
			// We're only creating the level here - insertion of the item into the new level
			// happens in the next iteration of the loop
			newLevel := &node[T]{}
			pos.up = newLevel
			newLevel.down = pos
			list.levels = newLevel
			levelCreated = true
		}

		pos = pos.up

		if pos.value != nil && list.compare(*pos.value, value) > 0 {
			// We've moved up a level, but the new current position's value is greater than the value to insert
			// (which can happen at higher levels, depending on insertion order and coin flips), so we need to move
			// back to the previous node in this level in order to be at the right spot.
			pos = pos.prev
		}
	}
}

func coinFlip() bool {
	return rand.Intn(2) == 0
}

func (list *SkipList[T]) Search(value T) (*node[T], bool) {
	pos := list.levels

	for level := list.levels; level != nil; {
		for node := level; node != nil; node = node.next {
			pos = node

			if node.next != nil && list.compare(*node.next.value, value) > 0 {
				break
			}
		}

		level = pos.down
	}

	if pos.value != nil && list.compare(*pos.value, value) == 0 {
		return pos, true
	}

	return nil, false
}

func (list *SkipList[T]) Delete(value T) {
	pos, found := list.Search(value)
	if !found {
		return
	}

	// Delete item from all layers
	for pos != nil {
		if pos.prev != nil {
			pos.prev.next = pos.next
		}

		if pos.next != nil {
			pos.next.prev = pos.prev
		}

		pos = pos.up
	}

	// Now we look for empty layers and remove them (this should only ever happen at the top level)
	for list.levels != nil && list.levels.next == nil {
		list.levels = list.levels.down
		if list.levels != nil {
			list.levels.up = nil
		}
	}
}

func (list *SkipList[T]) Display() {
	if list.levels == nil {
		fmt.Println("Skip list is empty")
		return
	}

	for cl := list.levels; cl != nil; cl = cl.down {
		for node := cl.next; node != nil; node = node.next {
			fmt.Print(*node.value, " -> ")
		}
		fmt.Println()
	}
}

// Rebalancing stuff - see https://en.wikipedia.org/wiki/Skip_list#Implementation_details
// Indexing stuff - see https://en.wikipedia.org/wiki/Skip_list#Indexable_skiplist

// make all nodes level 1
// j ← 1
// while the number of nodes at level j > 1 do
//     for each i'th node at level j do
//         if i is odd and i is not the last node at level j
//             randomly choose whether to promote it to level j+1
//         else if i is even and node i-1 was not promoted
//             promote it to level j+1
//         end if
//     repeat
//     j ← j + 1
// repeat
