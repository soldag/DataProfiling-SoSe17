# Spidey Documentation
by Sören Oldag & Tamara Slosarek

&nbsp;

Spidey (SPIDER[1], basically) is an algorithm that finds all valid, unary inclusion dependencies (INDs) of a data set. Let A ⊆ B be an IND, then A is the dependent and B the referenced attribute.

[1] Bauckmann, J., Leser, U., Naumann, F., & Tietz, V. (2007, April). Efficiently detecting inclusion dependencies. In _Data Engineering, 2007. ICDE 2007. IEEE 23rd International Conference on_ (pp. 1448-1450). IEEE.

## Algorithm

The algorithm works like SPIDER, but completely in-memory. We first sort all distinct values for each column in a min-heap. Additionally, for each column as a dependent attribute, we initialize a set of possible referenced attributes, containing all columns but the dependent column itself.

Until all but one heaps are empty, we choose the minimum value of all heaps. Then, we collect all columns that contain that value in a set, while removing the value. For each of these columns, the set of referenced attributes is updated by intersecting it with the set of just found columns.

In the end, for each dependent attribute unary INDs are generated from the set of referenced attributes.

## Experiments

Used machine: Windows 10, 8 GB RAM, Intel Core i7 @ 2.40 GHz

Metanome settings: 6144 MB

Web Data Commons measurements are averages from three runs, TPC-H 10 measurements were executed once.

| Dataset          | Size    | Attributes | INDs    | Found INDs | Runtime SPIDER   | Runtime spidey                          |
|------------------|---------|------------|---------|------------|------------------|-----------------------------------------|
| Web Data Commons | 21.8 KB | 57         | 112     | 112        | 588 ms           | 161 ms                                  |
| TPC-H 10 1k      | 763 KB  | 61         | 34      | 34         | 629 ms           | 785 ms                                  |
| TPC-H 10         | 13.8 GB | 61         | 66      |            | 01 h 19 min 27 s | Cancelled after 20 h                    |

For a relatively small dataset, spidey performs better than SPIDER, since it does not have the overhead of file operations but works in-memory. For larger sets we assume that SPIDER uses more elaborated data structures and therefore performs better than spidey. For bigger datasets, we would have thought that spidey runs out of memory pretty fast, however, we cancelled the execution after 20 hours.
