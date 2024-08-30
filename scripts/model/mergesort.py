'''
Basic steps for merge sort algorithm
1. Divide and conquer approach: it uses the divide and conquer paradigm. it means
  that the initial problem is divided into associated subproblems, where a solution
  can be applied recursively to each subproblem. The final solution to the larger
  problem is a combination of the solutions to the subproblems.
2. Divide: continously break down the larger problem into smaller parts.
3. Conquer: solve each of the smaller parts by utilizing a function that's called recursively.
4. Combine: merge all solutions for all smaller parts into a single unified solution,
  which becomes the solution to the starting problem.
'''

# merge sort algo
def merge_sort(arr):
    if len(arr) > 1:
        mid = len(arr) // 2  # mid position
        left_half = arr[:mid]  # left half
        right_half = arr[mid:]  # right half

        merge_sort(left_half)  # iterate left half
        merge_sort(right_half)  # iterate right half

        # merge left half and right half
        i = j = k = 0
        while i < len(left_half) and j < len(right_half):
            if left_half[i] < right_half[j]:
                arr[k] = left_half[i]
                i += 1
            else:
                arr[k] = right_half[j]
                j += 1
            k += 1

        # check if leftover elements in left half and right half
        while i < len(left_half):
            arr[k] = left_half[i]
            i += 1
            k += 1
        while j < len(right_half):
            arr[k] = right_half[j]
            j += 1
            k += 1

# test
arr = [54, 26, 93, 17, 77, 31, 44, 55, 20]
merge_sort(arr)
print("merged array：", arr)
