# Spidey Documentation
by Sören Oldag & Tamara Slosarek

&nbsp;

Spidey (SPIDER[1], basically) is an algorithm that finds all valid, unary inclusion dependencies (INDs) of a data set.

[1] Bauckmann, J., Leser, U., Naumann, F., & Tietz, V. (2007, April). Efficiently detecting inclusion dependencies. In _Data Engineering, 2007. ICDE 2007. IEEE 23rd International Conference on_ (pp. 1448-1450). IEEE.

## Algorithm

The algorithm works like SPIDER but completely in-memory. We first sort all distinct values for each column in a min-heap. Additionally, we initialize a list of possible references for each column that includes all other columns.

Then, until all heaps are empty (but one last entry we do not need to check anymore), we choose the minimum of all heaps. Then, we collect all columns that contain the minimum and remove this first entry.

For these columns, the references are updated. This is done by intersecting the corresponding reference set with the set containing the found columns.

In the end, unary INDs are generated from the list of reference sets.

## Experiments

Used machine: Windows 10, 8 GB RAM, Intel Core i7 @ 2.40 GHz

Metanome settings: 6144 MB

Web Data Commons measurements are averages from three runs, TPC-H 10 measurements were executed once.

| Dataset          | Size    | Attributes | INDs    | Found INDs | Runtime SPIDER | Runtime spidey |
|------------------|---------|------------|---------|------------|----------------|----------------|
| Web Data Commons | 21.8 KB | 57         | 112     | 112        | 588 ms         | 161 ms         |
| TPC-H 10         | 100 GB  | 61         | 90      | 112        | 12 min 17 s    |                |

// TODO: search for other dataset, run nc-voter, maybe add inclusion dependencies manually (or use nc-voter 1k)

For a small dataset spidey performs better than SPIDER, since it does not have the overhead of file operations but works in-memory.
