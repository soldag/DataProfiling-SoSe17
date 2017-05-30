# Taney Documentation
by Sören Oldag & Tamara Slosarek

&nbsp;

Taney (tiny TANE) is an algorithm that finds the valid, minimal functional dependencies (FDs) of a data set.

## Algorithm

Taney, like TANE [1], uses partitioning and a bottom-up a priori approach with candidate pruning.

### Partitioning

* using PLIs

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
