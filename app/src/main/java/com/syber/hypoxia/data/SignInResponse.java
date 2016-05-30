package com.syber.hypoxia.data;

import com.syber.base.data.BaseResponse;


/**
 * Created by liangtg on 16-5-11.
 */
public class SignInResponse extends BaseResponse {

    public UserInfo userinfo;

    public String sessionid;

    public UserInfoExt userinfoExt;

    @Override
    public String toString() {
        return "SignInResponse{" +
                "userinfo=" + userinfo +
                ", sessionid='" + sessionid + '\'' +
                ", userinfoExt=" + userinfoExt +
                '}';
    }

    public static class UserInfo {
        public String id;
        public String user_name;
        public String pswd;
        public String salt;
        public String email;
        public String create_time;
        public String Ext;

        @Override
        public String toString() {
            return "UserInfo{" +
                    "id='" + id + '\'' +
                    ", user_name='" + user_name + '\'' +
                    ", pswd='" + pswd + '\'' +
                    ", salt='" + salt + '\'' +
                    ", email='" + email + '\'' +
                    ", create_time='" + create_time + '\'' +
                    ", Ext='" + Ext + '\'' +
                    '}';
        }
    }

    public static class UserInfoExt {
        public String user_id;
        public String fullname;
        public String sex;
        public String sexstring;
        public String birthday;
        public String height;
        public String weight;
        public String blood;
        public String bloodstring;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserInfoExt that = (UserInfoExt) o;

            if (fullname != null ? !fullname.equals(that.fullname) : that.fullname != null) return false;
            if (sex != null ? !sex.equals(that.sex) : that.sex != null) return false;
            if (height != null ? !height.equals(that.height) : that.height != null) return false;
            return weight != null ? weight.equals(that.weight) : that.weight == null;

        }

        @Override
        public int hashCode() {
            int result = fullname != null ? fullname.hashCode() : 0;
            result = 31 * result + (sex != null ? sex.hashCode() : 0);
            result = 31 * result + (height != null ? height.hashCode() : 0);
            result = 31 * result + (weight != null ? weight.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "UserInfoExt{" +
                    "user_id='" + user_id + '\'' +
                    ", fullname='" + fullname + '\'' +
                    ", sex='" + sex + '\'' +
                    ", sexstring='" + sexstring + '\'' +
                    ", birthday='" + birthday + '\'' +
                    ", height='" + height + '\'' +
                    ", weight='" + weight + '\'' +
                    ", blood='" + blood + '\'' +
                    ", bloodstring='" + bloodstring + '\'' +
                    '}';
        }
    }

}
