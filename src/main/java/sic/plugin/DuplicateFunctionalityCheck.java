package sic.plugin;

import com.sun.javadoc.*;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("unused")
public class DuplicateFunctionalityCheck {

    @SuppressWarnings("unused")
    public static boolean start(RootDoc root) throws IOException, ParseException {
        checkForDuplicates(root);
        return true;
    }

    private static void checkForDuplicates(RootDoc root) {
        ClassDoc[] classes = root.classes();
        Map<Doc, Tag> allPurposeTags = getAllPurposeTags(root);
        for (ClassDoc classDoc : classes) {
            MethodDoc[] methods = classDoc.methods();
            for (MethodDoc methodDoc : methods) {
                Tag[] purposeTagsForMethod = methodDoc.tags("@sic.purpose");
                if (purposeTagsForMethod.length == 0) {
                    continue;
                }
                String purposeOfMethod = purposeTagsForMethod[0].text();
                for (Tag tag : allPurposeTags.values()) {
                    if (tag.holder() == methodDoc) {
                        continue;
                    }
                    if (isSimilar(tag.text(), purposeOfMethod)) {
                        Doc holder = tag.holder();
                        if (tag.holder() instanceof MethodDoc) {
                            System.out.println("[Duplicate Warning] " + methodDoc.name() + "() might be containing a duplicate functionality");
                            System.out.println("[Duplicate Warning] " + holder.name() + "() method in Class " + ((MethodDoc) holder).containingClass() + " contains method with a similar purpose");
                            System.out.println("[Duplicate Warning] if those two are same please refactor to use one only one, if those two are not the same consider having more unique purpose messages");
                            if (((MethodDoc) tag.holder()).containingClass() != methodDoc.containingClass()) {
                                System.out.println("[Duplicate Warning] if they are not the same but relevant consider having them in the same class/package with unique purpose messages\n");
                            }
                        }

                    }
                }
            }
        }
    }

    private static boolean isSimilar(String text1, String text2) {
        HashSet<String> set1 = new HashSet<String>(Arrays.asList(text1.split(" ")));
        HashSet<String> set2 = new HashSet<String>(Arrays.asList(text2.split(" ")));

        List<String> stopWords = new ArrayList<String>(Arrays.asList("in", "a", "an", "to", "with", "and", "of"));

        set1.removeAll(stopWords);
        set2.removeAll(stopWords);

        int set1Size = set1.size();
        set1.removeAll(set2);

        return (Math.abs(set1.size() - set1Size) * 100.0 / set1Size) > 70;
    }

    private static Map<Doc, Tag> getAllPurposeTags(RootDoc root) {
        ClassDoc[] classes = root.classes();
        Map<Doc, Tag> map = new HashMap<Doc, Tag>();
        for (ClassDoc classDoc : classes) {
            MethodDoc[] methods = classDoc.methods();
            for (MethodDoc methodDoc : methods) {
                Tag[] purposeTagsForMethod = methodDoc.tags("@sic.purpose");
                if (purposeTagsForMethod.length == 0) {
                    continue;
                }
                map.put(classDoc, purposeTagsForMethod[0]);
            }
        }
        return map;
    }
}
