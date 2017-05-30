# Taney Documentation
by Sören Oldag & Tamara Slosarek

&nbsp;

Taney (tiny TANE) is an algorithm that finds the valid, minimal functional dependencies (FDs) of a data set.

## Algorithm

Taney uses partitioning and a bottom-up a priori approach with candidate pruning, which among others are used by TANE [1].

### Partitioning

The partitioning step considers the distinct values (partitions, equivalence classes) of a column combination. If every partition π_X of one column combination X is a subset of some partition π_A of column A, π_X refines π_A. If π_X refines π_A, X depends A functionally. Since partitions of size one cannot violate any functional dependency, they can be ignored. Then the key error has to be used, which states the number of tuples to be removed for a column combination to be key.

For the implementation we used the PLIs from the Metanome algorithm helpers, since they exactly describe the behavior of stripped partitions and already implement the needed functions intersect() and getRawKeyError().

### A Priori with Candidate Pruning

* ...

[1] Huhtala, Ykä, et al. "TANE: An efficient algorithm for discovering functional and approximate dependencies." _The computer journal_ 42.2 (1999): 100-111.

## Experiments

Used machine: Windows 10, 8 GB RAM, Intel Core i7 @ 2.40 GHz
Metanome settings: 2048 MB

| Dataset       | Columns | Rows   | Size   | FDs     | Found FDs | Runtime FUN | Runtime TANE | Runtime taney |
|---------------|---------|--------|--------|---------|-----------|-------------|--------------|---------------|
| iris          | 5       | 150    | 5 KB   | 4       |           |             |              |               |
| balance-scale | 5       | 625    | 7 KB   | 1       |           |             |              |               |
| chess         | 7       | 28.056 | 519 KB | 1       |           |             |              |               |
| letter        | 16      | 20.000 | 695 KB | 61      |           |             |              |               |
| flight_1k     | 109     | 1.000  | 575 KB | 982.631 |           |             |              |               |

_Did you discover any limitations of your approach (e.g. runtime or memory consumption)
that made computing a certain dataset impossible?_
