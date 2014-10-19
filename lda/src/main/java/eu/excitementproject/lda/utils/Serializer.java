package eu.excitementproject.lda.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Writes/loads objects to/from serialized files 
 * @author Eyal Shnarch
 * @since 07/09/2011
 */
public class Serializer<T> {
        
        public void serialize(T object, String path) throws FileNotFoundException, IOException{
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
                try
                {
                        oos.writeObject(object);
                }
                finally
                {
                        oos.close();
                }
        }
        
        @SuppressWarnings("unchecked")
        public T load(String path) throws FileNotFoundException, IOException, ClassNotFoundException{
                T object = null;
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
                try
                {
                        object = (T)ois.readObject();
                        return object;
                }
                finally
                {
                        ois.close();
                }
        }
}