package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Convenience class for position like typed regions.
 */
public class TypedPosition extends Position {
	
	/** The type of the region described by this position */
	private String fType;
	
	/**
	 * Creates a position along the given specification.
	 *
	 * @param offset the offset of this position
	 * @param length the length of this position
	 * @param type the type of this position
	 */
	public TypedPosition(int offset, int length, String type) {
		super(offset, length);
		fType= type;
	}
	
	/**
	 * Creates a position based on the typed region.
	 *
	 * @param region the typed region
	 */
	public TypedPosition(ITypedRegion region) {
		super(region.getOffset(), region.getLength());
		fType= region.getType();
	}
	
	/**
	 * Returns the type of the position
	 *
	 * @return the type of this position
	 */	
	public String getType() {
		return fType;
	}
	
	/*
	 * @see Object#equals
	 */
	public boolean equals(Object o) {
		if (o instanceof TypedPosition) {
			if (super.equals(o)) {
				TypedPosition p= (TypedPosition) o;
				return (fType == null && p.getType() == null) || fType.equals(p.getType());
			}
		}
		return false;
	}
	
	/*
	 * @see Object#hashCode
	 */
	 public int hashCode() {
	 	int type= fType == null ? 0 : fType.hashCode();
	 	return super.hashCode() | type;
	 }
}