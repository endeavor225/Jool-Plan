package com.jool.plugin.mapping.mapping;

//----------------------------------------------------------------------------------------
//	Copyright © 2007 - 2020 Tangible Software Solutions, Inc.
//	This class can be used by anyone provided that the copyright notice remains intact.
//
//	This class is used to convert some of the .NET List methods to Java.
//----------------------------------------------------------------------------------------

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public final class ListHelper
{
	public static <T> boolean exists(CopyOnWriteArrayList<T> source, Predicate<T> predicate)
	{
		for (T element : source)
		{
			if (predicate.test(element))
				return true;
		}

		return false;
	}

	public static <T> T find(CopyOnWriteArrayList<T> source, Predicate<T> predicate)
	{
		for (T element : source)
		{
			if (predicate.test(element))
				return element;
		}

		return null;
	}

	public static <T> CopyOnWriteArrayList<T> findAll(CopyOnWriteArrayList<T> source, Predicate<T> predicate)
	{
		CopyOnWriteArrayList<T> dest = new CopyOnWriteArrayList<T>();

		for (T element : source)
		{
			if (predicate.test(element))
				dest.add(element);
		}

		return dest;
	}

	public static <T> int findIndex(CopyOnWriteArrayList<T> source, Predicate<T> predicate)
	{
		for (int i = 0; i < source.size(); i++)
		{
			if (predicate.test(source.get(i)))
				return i;
		}

		return -1;
	}

	public static <T> int findIndex(CopyOnWriteArrayList<T> source, int start, Predicate<T> predicate)
	{
		for (int i = start; i < source.size(); i++)
		{
			if (predicate.test(source.get(i)))
				return i;
		}

		return -1;
	}

	public static <T> int findIndex(CopyOnWriteArrayList<T> source, int start, int count, Predicate<T> predicate)
	{
		for (int i = start; i < start + count; i++)
		{
			if (predicate.test(source.get(i)))
				return i;
		}

		return -1;
	}

	public static <T> T findLast(CopyOnWriteArrayList<T> source, Predicate<T> predicate)
	{
		for (int i = source.size() - 1; i > -1; i--)
		{
			if (predicate.test(source.get(i)))
				return source.get(i);
		}

		return null;
	}

	public static <T> int findLastIndex(CopyOnWriteArrayList<T> source, Predicate<T> predicate)
	{
		for (int i = source.size() - 1; i > -1; i--)
		{
			if (predicate.test(source.get(i)))
				return i;
		}

		return -1;
	}

	public static <T> int findLastIndex(CopyOnWriteArrayList<T> source, int start, Predicate<T> predicate)
	{
		for (int i = start; i > -1; i--)
		{
			if (predicate.test(source.get(i)))
				return i;
		}

		return -1;
	}

	public static <T> int findLastIndex(CopyOnWriteArrayList<T> source, int start, int count, Predicate<T> predicate)
	{
		for (int i = start; i > start - count; i--)
		{
			if (predicate.test(source.get(i)))
				return i;
		}

		return -1;
	}

	public static <T> int removeAll(CopyOnWriteArrayList<T> source, Predicate<T> predicate)
	{
		int removed = 0;
		for (int i = 0; i < source.size(); i++)
		{
			if (predicate.test(source.get(i)))
			{
				source.remove(i);
				i--;
				removed++;
			}
		}

		return removed;
	}

	public static <T> boolean trueForAll(CopyOnWriteArrayList<T> source, Predicate<T> predicate)
	{
		for (T element : source)
		{
			if ( ! predicate.test(element))
				return false;
		}

		return true;
	}
}