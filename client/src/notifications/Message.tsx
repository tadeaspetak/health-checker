import { useTimeout } from "../utils";

import { useNotifications, Notification, NotificationType } from "./Ctx";

const typeToColor = (type: NotificationType) => {
  switch (type) {
    case "error":
      return "red";
    case "success":
      return "emerald";
    default:
      return "lightBlue";
  }
};
export const Message: React.FC<{ notification: Notification }> = ({
  notification,
}) => {
  const { remove } = useNotifications();
  const { id, message, type } = notification;

  useTimeout(() => remove(id), 5000);

  return (
    <div
      title="Close the notification"
      className={`relative px-6 py-4 mb-4 text-white border-0 rounded cursor-pointer bg-${typeToColor(
        type,
      )}-500`}
      onClick={() => {
        remove(id);
      }}
    >
      <span className="inline-block mr-8 align-middle">{message}</span>
      <button className="absolute top-0 right-0 mt-4 mr-6 text-2xl font-semibold leading-none bg-transparent outline-none focus:outline-none">
        <span>×</span>
      </button>
    </div>
  );
};
