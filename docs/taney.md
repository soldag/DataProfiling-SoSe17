# Taney Documentation
by Sören Oldag & Tamara Slosarek

&nbsp;

Taney (tiny TANE[1]) is an algorithm that finds all valid, minimal functional dependencies (FDs) of a data set.

[1] Huhtala, Ykä, et al. "TANE: An efficient algorithm for discovering functional and approximate dependencies." _The computer journal_ 42.2 (1999): 100-111.

## Algorithm

Taney uses partitioning and a bottom-up a priori approach with basic pruning.

### A Priori

Each node of the lattice represents all possible columns X for the FD. By that, we only allow non-trivial FDs, since for a column A chosen as the right-hand side of the FD (RHS), its left-hand side (LHS) is X\\A.

The lattice is traversed bottom-up. To assure minimal FDs, pruning is used: A rule X\\B → B is not minimal and does not need to be checked when there already is a FD Y\\B → B with Y ⊂ X.

TANE uses candidate pruning for this, which basically does the same thing but is more elaborated and faster for large datasets.

### Partitioning

The partitioning step is fully adopted from TANE and considers the distinct values, partitions, of a column combination. If every partition π<sub>X</sub> of one column combination X is a subset of some partition π<sub>A</sub> of column A, π<sub>X</sub> refines π<sub>A</sub>. If π<sub>X</sub> refines π<sub>A</sub>, A depends functionally on X.

Since partitions of size one cannot violate any functional dependency, they can be ignored. Then the key error has to be used, which states the number of tuples to be removed for a column combination to be key.

For the implementation we used PLIs provided by the Metanome algorithm helpers, since they exactly describe the behavior of stripped partitions and already implement the needed functions intersect() and getRawKeyError().

## Experiments

Used machine: Windows 10, 8 GB RAM, Intel Core i7 @ 2.40 GHz

Metanome settings: 2048 MB (6144 MB for letter)

All runtime measurements are averages from three runs (letter-1k only one run).

| Dataset       | Columns | Rows   | Size   | FDs     | Found FDs | Runtime FUN | Runtime TANE | Runtime taney |
|---------------|---------|--------|--------|---------|-----------|-------------|--------------|---------------|
| iris          | 5       | 150    | 5 KB   | 4       | 4         | 40 ms       | 240 ms       | 95 ms         |
| balance-scale | 5       | 625    | 7 KB   | 1       | 1         | 61 ms       | 255 ms       | 133 ms        |
| chess         | 7       | 28.056 | 519 KB | 1       | 1         | 155 ms      | 500 ms       | 478 ms        |
| bridges       | 13      | 108    | 6 KB   | 142     | 142       | 380 ms      | 543 ms       | 648 ms        |
| letter        | 16      | 20.000 | 695 KB | 61      | -         | Error (*)   | Error (*)    | Error (*)     |
| letter-1k     | 16      | 1.000  | 36 KB  | 4758    | 4758      | 156.413 ms  | 6.975 ms     | 79.956 ms     |

(*) OutOfMemoryError: GC overhead limit exceeded

With more columns, the runtime increases rapidly. Additionally, with many rows, the memory consumption is too high for our system to handle (for all three algorithms).
