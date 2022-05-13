package message;

import java.io.Serializable;

public class Message implements Serializable, MessageTypes {
      
        int type;
        Object content;

        public Message(int type, Object content) {
            this.type = type;
            this.content = content;
        }
        
        public int getType() {
            return type;
        }
        
        public Object getContent() {
            return content;
        }
        
        public void setType(int type) {
            this.type = type;
        }
        
        public void setContent(Object content) {
            this.content = content;
        }
}
