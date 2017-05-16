# SUCCESS Documentation

**_Describe the algorithm’s basic idea. How does the algorithm cope with the complexity of the given task?_**

SUCCESS (**S**uper **UCC**... w**e** don't know what the re**s**t of the acronym **s**tands for) is an algorithm that finds the minimal unique column combinations (UCCs) of a data set.

It is an **Apriori** approach traversing the corresponding lattice and pruning when an UCC is found.

It works with a queue of column combinations that first holds the single columns. While the queue contains combinations, the algorithm extracts the first one and checks its uniqueness.

For the uniqueness-check a **hash** (Java's HashSet implementation) is used. Additionally, since _NULL ≠ NULL_, if a row contains a NULL value in any of the columns of the combination, it is ignored for the check (see below).

If a combination is an UCC, it is added to the result list and the algorithm proceeds with the next combination in the queue. If not, all successors in the lattice are generated and appended to the queue. For each generated combination it is checked, whether it contains any already detected UCC. In this case the algorithm also proceeds with the next combination since the current one is not minimal.

**_If you used an algorithm from literature, provide a reference to the according publication._**

[Nope.](https://media.giphy.com/media/3og0IwGidh5DYVDnzi/giphy.gif)

**_If you came up with an own approach, provide one or two arguments why it is or could be better than related algorithms._**

We started working at this algorithm right when the exercise was published, so we had no prior knowledge of reference algorithms and just wanted to perform better than the brute force approach.

In retrospective we could have used **Position List Indices** to speed up the uniqueness-check.

**_If your algorithm implements an adaption or optimization of existing approaches, describe these briefly._**

[Nope.](https://media.giphy.com/media/kGCuRgmbnO9EI/giphy.gif)

**_If you solved a bonus task, please discuss your findings here as well._**

[Nope.](https://media.giphy.com/media/3oeSAYNUIwvGwl5RRK/giphy.gif)

**_How many unique column combinations did your algorithm find on the provided datasets?_**

* WDC_planets: **7**
* WDC_satellites: **4**

**_How long did the discovery take on each dataset and what machine did you use?_**

Execution on Metanome with 2048 MB, used macOS Sierra with i5 @ 1.8 GHz. The results are the average run times of five runs.

* WDC_planets: **69.8 ms**
* WDC_satellites: **185.2 ms**

For comparison, HyUCC performs the task on WDC_planets in 63 ms and WDC_satellites in 92 ms.

****_Did you discover any limitations of your approach (e.g. runtime or memory consumption) that made computing a certain dataset impossible?_**

[Nope.](https://media.giphy.com/media/W5YVAfSttCqre/giphy.gif)

**_What is the conceptual differences between NULL ≠ NULL and NULL = NULL and how does the choice of the NULL semantic influence the performance of your algorithm?_**

As far as we understood, _NULL ≠ NULL_ means that all NULL values are unique, which means that a row of a column combination that contains a NULL value automatically is unique.

Our implementation (see below) filters out rows containing NULL values before testing its uniqueness with the hash, which reduces the number of rows that need to be hashed. With _NULL = NULL_ we could skip this filter process but would need to consider all rows for the hashing. The latter would probably be a bit faster but the complexity stays the same.

```java
// Put the regarding lines of our records together in one string.
// Filter for rows containing NULL values and ignore them since NULL != NULL.
List<String> projectedRecords = records.stream()
	.map(row -> this.projectRow(row, columnIndices))
	.filter(row -> !row.contains(null))
	.map(row -> row.stream().collect(Collectors.joining("|")))
	.collect(Collectors.toList());

// Store the rows in a hash set. If two values collide, break.
Set<String> uniqueRows = new HashSet<>(projectedRecords.size());
for (String row : projectedRecords) {
	if (!uniqueRows.add(row)) {
		return false;
	}
}
```
