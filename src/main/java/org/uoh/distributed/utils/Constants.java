package org.uoh.distributed.utils;

public class Constants
{


  /** Need to set the bootstrap IP and Port here */
  public static int BOOTSTRAP_PORT = 55555;
  public static String BOOTSTRAP_IP = "127.0.0.1";

  /** Message format to be used when sending a request to the bootstrap server. ${length} ${msg} */
  public static final String MSG_SEPARATOR = "~";
  public static final String MSG_FORMAT = "%04d?%s".replace( "?",MSG_SEPARATOR );
  public static final String REG = "REG";
  public static final String UNREG = "UNREG";
  public static final String REGOK = "REGOK";
  public static final String UNREGOK = "UNREGOK";
  public static final String ECHO = "ECHO";
  public static final String ECHOOK = "ECHOOK";

  /** Types of syncs */
  public static final String TYPE_ROUTING = "RTBL";
  public static final String TYPE_ENTRIES = "ETBL";

  /** Message commands to be used in client server communications **/
  public static final String GET_ROUTING_TABLE = "GETRTBL";
  public static final String NEW_NODE = "NEWNODE";
  public static final String RESPONSE_OK = "OK";
  public static final String RESPONSE_FAILURE = "FAILED";

  /** SYNC - sync the entry table entries by handing over anything that should belong to that node */
  public static final String SYNC = "SYNC";
  public static final String PING = "PING";

  /** REG ${ip} ${port} ${username} */
  public static final String REG_MSG_FORMAT = (REG+"?%s?%d?%s").replace( "?", MSG_SEPARATOR );
  /** UNREG ${ip} ${port} ${username} */
  public static final String UNREG_MSG_FORMAT = (UNREG+"?%s?%d?%s").replace( "?", MSG_SEPARATOR );
  /** NEWNODE ${ip} ${port} ${nodeId} */
  public static final String NEWNODE_MSG_FORMAT = (NEW_NODE +"?%s?%d?%d").replace( "?", MSG_SEPARATOR );
  /** PING - Pings and gets the entries of the corresponding node */
  public static final String PING_MSG_FORMAT = (PING+"?%d?%s").replace( "?", MSG_SEPARATOR );
  /** SYNC ${type} ${serialized_object} - For syncing table entries and routing tables */
  public static final String SYNC_MSG_FORMAT = (SYNC+"?%s?%s").replace( "?", MSG_SEPARATOR );


  /** Status Codes **/
  public static final int E0000 = 0;    // No nodes in the network
  public static final int E0001 = 1;    // 1 node in the network
  public static final int E0002 = 2;    // 2 nodes in the network
  public static final int E0003 = 3;    // 3 nodes in the network
  public static final int E9999 = 9999; // Error in command
  public static final int E9998 = 9998; // Already registered
  public static final int E9997 = 9997; // Port not available
  public static final int E9996 = 9996; // Network Full





  /** How many times a given UDP request be retried */
  public static final int BOOTSTRAP_RETRIES_COUNT = 5;
  public static final int RETRIES_COUNT = 5;
  public static final int RETRY_TIMEOUT_MS = 5000;
  public static final int GRACE_PERIOD_MS = 5000;
  public static final int HEARTBEAT_FREQUENCY_MS = 20000;
  public static final int HEARTBEAT_INITIAL_DELAY = 30000;


  public static final int ADDRESS_SPACE_SIZE = 180;


}
