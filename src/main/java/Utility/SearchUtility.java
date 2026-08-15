/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utility;

import ADT.ArrayList;
import ADT.ListInterface;

public class SearchUtility {

    // ============ Generic Linear Search ============

    public static <T> int linearSearch(ListInterface<T> list, T target) {

        for (int i = 1; i <= list.size(); i++) {

            if (list.getEntry(i).equals(target)) {
                return i;
            }
        }

        return -1;
    }


    // Linear search for normal array
    public static <T> int linearSearch(T[] array, T target) {

        for (int i = 0; i < array.length; i++) {

            if (array[i].equals(target)) {
                return i;
            }
        }

        return -1;
    }


    // ============ Binary Search ============

    public static <T extends Comparable<T>> int binarySearch(ListInterface<T> list, T target) {

        int low = 1;
        int high = list.size();

        while (low <= high) {

            int mid =
                    low + (high - low) / 2;

            T midValue =
                    list.getEntry(mid);

            int cmp =
                    midValue.compareTo(target);

            if (cmp == 0) {
                return mid;

            } else if (cmp < 0) {
                low = mid + 1;

            } else {
                high = mid - 1;
            }
        }

        return -1;
    }


    // Binary search for normal array
    public static <T extends Comparable<T>> int binarySearch(T[] array, T target) {

        int low = 0;
        int high = array.length - 1;

        while (low <= high) {

            int mid =
                    low + (high - low) / 2;

            int cmp =
                    array[mid].compareTo(target);

            if (cmp == 0) {
                return mid;

            } else if (cmp < 0) {
                low = mid + 1;

            } else {
                high = mid - 1;
            }
        }

        return -1;
    }


    // ============ Search by Custom Condition ============

    public static <T> int findByCriteria(ListInterface<T> list, SearchCriteria<T> criteria) {

        for (int i = 1; i <= list.size(); i++) {

            if (criteria.matches(
                    list.getEntry(i))) {

                return i;
            }
        }

        return -1;
    }


    // Find all matching items
    public static <T> ListInterface<T> findAllByCriteria(ListInterface<T> list, SearchCriteria<T> criteria) {

        ListInterface<T> result =
                new ArrayList<>();

        for (int i = 1; i <= list.size(); i++) {

            T item =
                    list.getEntry(i);

            if (criteria.matches(item)) {
                result.add(item);
            }
        }

        return result;
    }


    // Condition used for custom searching
    public interface SearchCriteria<T> {

        boolean matches(T item);
    }
}