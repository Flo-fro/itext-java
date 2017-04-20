package com.itextpdf.kernel.pdf.tagutils;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.tagging.IPdfStructElem;
import com.itextpdf.kernel.pdf.tagging.PdfStructElem;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is used to manage waiting tags status.
 * Any tag in the structure tree could be marked as "waiting". This status indicates that
 * tag is not yet finished and therefore should not be flushed or removed if page tags are
 * flushed or removed or if parent tags are flushed.
 * </p>
 * <p>
 * Waiting status of tags is defined by the association with arbitrary objects instances.
 * This mapping is one to one: for every waiting tag there is always exactly one associated object.
 * </p>
 * Waiting status could also be perceived as a temporal association of the object to some particular tag.
 */
public class WaitingTagsManager {

    private Map<Object, PdfStructElem> associatedObjToWaitingTag;
    private Map<PdfDictionary, Object> waitingTagToAssociatedObj;

    WaitingTagsManager() {
        associatedObjToWaitingTag = new HashMap<>();
        waitingTagToAssociatedObj = new HashMap<>();
    }

    /**
     * Assigns waiting status to the tag at which given {@link TagTreePointer} points, associating it with the given
     * {@link Object}. If current tag of the given {@link TagTreePointer} is already waiting, then after this method call
     * it's associated object will change to the one passed as the argument and the old one will not longer be
     * an associated object.
     * @param pointer a {@link TagTreePointer} pointing at a tag which is desired to be marked as waiting.
     * @param associatedObj an object that is to be associated with the waiting tag. A null value is forbidden.
     * @return the previous associated object with the tag if it have already had waiting status,
     * or null if it was not waiting tag.
     */
    public Object assignWaitingTagStatus(TagTreePointer pointer, Object associatedObj) {
        if (associatedObj == null) { throw new NullPointerException(); }
        return saveAssociatedObjectForWaitingTag(associatedObj, pointer.getCurrentStructElem());
    }

    /**
     * Checks if there is waiting tag which status was assigned using given {@link Object}.
     * @param obj an {@link Object} which is to be checked if it is associated with any waiting tag. A null value is forbidden.
     * @return true if object is currently associated with some waiting tag.
     */
    public boolean isObjectAssociatedWithWaitingTag(Object obj) {
        if (obj == null) { throw new NullPointerException(); }
        return associatedObjToWaitingTag.containsKey(obj);
    }

    /**
     * Moves given {@link TagTreePointer} to the waiting tag which is associated with the given object.
     * If the passed object is not associated with any waiting tag, {@link TagTreePointer} position won't change.
     * @param tagPointer a {@link TagTreePointer} which position in the tree is to be changed to the
     *                   waiting tag in case of the successful call.
     * @param associatedObject an object which is associated with the waiting tag to which {@link TagTreePointer} is to be moved.
     * @return true if given object is actually associated with the waiting tag and {@link TagTreePointer} was moved
     * in order to point at it.
     */
    public boolean movePointerToWaitingTag(TagTreePointer tagPointer, Object associatedObject) {
        if (associatedObject == null) return false;

        PdfStructElem waitingStructElem = associatedObjToWaitingTag.get(associatedObject);
        if (waitingStructElem != null) {
            tagPointer.setCurrentStructElem(waitingStructElem);
            return true;
        }
        return false;
    }

    /**
     * Gets an object that is associated with the tag (if there is one) at which given {@link TagTreePointer} points.
     * Essentially, this method could be used as indication that current tag has waiting status.
     * @param pointer a {@link TagTreePointer} which points at the tag for which associated object is to be retrieved.
     * @return an object that is associated with the tag at which given {@link TagTreePointer} points, or null if
     * current tag of the {@link TagTreePointer} is not a waiting tag.
     */
    public Object getAssociatedObject(TagTreePointer pointer) {
        return getObjForStructDict(pointer.getCurrentStructElem().getPdfObject());
    }

    /**
     * Removes waiting status of the tag which is associated with the given object.
     * <p>NOTE: if parent of the waiting tag is already flushed, the tag and it's children
     * (unless they are waiting tags on their own) will be also immediately flushed right after
     * the waiting status removal.</p>
     * @param associatedObject an object which association with the waiting tag is to be removed.
     * @return true if object was actually associated with some tag and it's association was removed.
     */
    public boolean removeWaitingTagStatus(Object associatedObject) {
        if (associatedObject != null) {
            PdfStructElem structElem = associatedObjToWaitingTag.remove(associatedObject);
            removeWaitingStatusAndFlushIfParentFlushed(structElem);
            return structElem != null;
        }
        return false;
    }

    /**
     * Removes waiting status of all waiting tags by removing association with objects.
     *
     * <p>NOTE: if parent of the waiting tag is already flushed, the tag and it's children
     * will be also immediately flushed right after the waiting status removal.</p>
     */
    public void removeWaitingStatusOfAllTags() {
        for (PdfStructElem structElem : associatedObjToWaitingTag.values()) {
            removeWaitingStatusAndFlushIfParentFlushed(structElem);
        }
        associatedObjToWaitingTag.clear();
    }

    PdfStructElem getStructForObj(Object associatedObj) {
        return associatedObjToWaitingTag.get(associatedObj);
    }

    Object getObjForStructDict(PdfDictionary structDict) {
        return waitingTagToAssociatedObj.get(structDict);
    }

    Object saveAssociatedObjectForWaitingTag(Object associatedObj, PdfStructElem structElem) {
        associatedObjToWaitingTag.put(associatedObj, structElem);
        return waitingTagToAssociatedObj.put(structElem.getPdfObject(), associatedObj);
    }

    /**
     * @return parent of the flushed tag
     */
    IPdfStructElem flushTag(PdfStructElem tagStruct) {
        Object associatedObj = waitingTagToAssociatedObj.remove(tagStruct.getPdfObject());
        if (associatedObj != null) {
            associatedObjToWaitingTag.remove(associatedObj);
        }

        IPdfStructElem parent = tagStruct.getParent();
        flushStructElementAndItKids(tagStruct);
        return parent;
    }

    private void flushStructElementAndItKids(PdfStructElem elem) {
        if (waitingTagToAssociatedObj.containsKey(elem.getPdfObject())) {
            return;
        }

        for (IPdfStructElem kid : elem.getKids()) {
            if (kid instanceof PdfStructElem) {
                flushStructElementAndItKids((PdfStructElem) kid);
            }
        }
        elem.flush();
    }

    private void removeWaitingStatusAndFlushIfParentFlushed(PdfStructElem structElem) {
        if (structElem != null) {
            waitingTagToAssociatedObj.remove(structElem.getPdfObject());
            if (structElem.getParent() == null) { // is flushed
                flushStructElementAndItKids(structElem);
            }
        }
    }
}
