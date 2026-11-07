const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_user_1";
const BOB_UID = "bob_user_2";
const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profiles or chats", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
  await assertFails(unauthDb.collection("chats").get());
});

test("Authenticated user: can create own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice Khan",
      username: "alicekhan",
      userCode: "AF123456",
      about: "Hello from Alice!",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create profile with another user ID", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      email: "bob@example.com",
      displayName: "Bob Khan",
      username: "bobkhan",
      userCode: "BK998877",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create contact request to another user", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("contact_requests").doc("req_1").set({
      fromUserId: ALICE_UID,
      toUserId: BOB_UID,
      fromUsername: "alicekhan",
      fromDisplayName: "Alice Khan",
      fromUserCode: "AF123456",
      status: "pending",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create chat and message where they are a member", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("chats").doc("chat_123").set({
      type: "direct",
      memberIds: [ALICE_UID, BOB_UID],
      lastMessage: "Hello Bob!",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("chats").doc("chat_123").collection("messages").doc("msg_1").set({
      chatId: "chat_123",
      senderId: ALICE_UID,
      senderName: "Alice Khan",
      text: "Hello Bob!",
      type: "text",
      status: "sent",
      createdAt: new Date(),
    })
  );
});
