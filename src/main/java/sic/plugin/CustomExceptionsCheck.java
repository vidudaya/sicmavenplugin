package sic.plugin;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.ThrowsTag;
import org.json.simple.parser.ParseException;

import java.io.IOException;

@SuppressWarnings("unused")
public class CustomExceptionsCheck {

    public static boolean start(RootDoc root) throws IOException, ParseException {
        validateExceptionsUsage(root);
        return true;
    }

    private static void validateExceptionsUsage(RootDoc root) {
        ClassDoc[] classes = root.classes();
        for (ClassDoc classDoc : classes) {
            MethodDoc[] methods = classDoc.methods();
            for (MethodDoc methodDoc : methods) {
                ThrowsTag[] throwsTags = methodDoc.throwsTags();
                for (ThrowsTag throwsTag : throwsTags) {
                    if (throwsTag.exceptionComment().startsWith("@sic.direct-throw")) {
                        if (throwsTag.exception().containingPackage() != methodDoc.containingPackage()) {
                            System.out.println("[Warning] " + throwsTag.exception() + " Exception thrown in " + classDoc.name() + " in " + classDoc.containingPackage());
                            System.out.println("\tA Custom Exception should not be directly thrown by another package");
                            System.out.println("\tA Custom Exception class should always be defined in the same package as the classes which are capable of throwing it");
                        }
                    }
                }
            }
        }
    }
}
