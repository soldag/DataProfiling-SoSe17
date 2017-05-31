# Taney Documentation
by Sören Oldag & Tamara Slosarek

&nbsp;

Taney (tiny TANE [1]) is an algorithm that finds the valid, minimal functional dependencies (FDs) of a data set.

## Algorithm

Taney uses partitioning and a bottom-up a priori approach with candidate pruning.

### Partitioning

The partitioning step considers the distinct values (partitions, equivalence classes) of a column combination. If every partition π_X of one column combination X is a subset of some partition π_A of column A, π_X refines π_A. If π_X refines π_A, X depends A functionally. Since partitions of size one cannot violate any functional dependency, they can be ignored. Then the key error has to be used, which states the number of tuples to be removed for a column combination to be key.

For the implementation we used the PLIs from the Metanome algorithm helpers, since they exactly describe the behavior of stripped partitions and already implement the needed functions intersect() and getRawKeyError().

### A Priori with Candidate Pruning

Each node of the lattice represents all possible columns X for the FD. By that, we only allow non-trivial FDs, since for a column A chosen as the right-hand side of the FD (RHS), its left-hand side (LHS) is X\\A.

The lattice is traversed bottom-up. To assure minimal FDs, candidate pruning is used: A rule X\\B → B is not minimal and does not need to be checked when there is already a FD Y\\B → B with Y ⊂ X.

_Is our pruning the same as the one of TANE?_
_Add candidate map to not excllude candidates on the fly?_

[1] Huhtala, Ykä, et al. "TANE: An efficient algorithm for discovering functional and approximate dependencies." _The computer journal_ 42.2 (1999): 100-111.

## Experiments

Used machine: Windows 10, 8 GB RAM, Intel Core i7 @ 2.40 GHz

Metanome settings: 2048 MB (6144 MB for letter)

All runtimemeasurements are averages from three runs.

| Dataset       | Columns | Rows   | Size   | FDs     | Found FDs | Runtime FUN | Runtime TANE | Runtime taney |
|---------------|---------|--------|--------|---------|-----------|-------------|--------------|---------------|
| iris          | 5       | 150    | 5 KB   | 4       |           | 40 ms       | 240 ms       |               |
| balance-scale | 5       | 625    | 7 KB   | 1       |           | 61 ms       | 255 ms       |               |
| chess         | 7       | 28.056 | 519 KB | 1       |           | 155 ms      | 500 ms       |               |
| bridges       | 13      | 108    | 6 KB   | 142     |           | 380 ms      | 543 ms       |               |
| letter        | 16      | 20.000 | 695 KB | 61      | -         | Error (*)   | Error (*)    |               |

(*) OutOfMemoryError: GC overhead limit exceeded

_Did you discover any limitations of your approach (e.g. runtime or memory consumption)
that made computing a certain dataset impossible?_
