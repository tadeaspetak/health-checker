import { Route, Switch } from "react-router-dom";

import { Messages } from "./notifications";
import { Services } from "./views";
import { ServiceProvider } from "./views/Services";

export const App = () => {
  return (
    <div className="min-h-full p-8 text-white bg-gray-800 sm:p-16">
      <Messages />
      <main className="w-full max-w-md m-auto overflow-hidden">
        <Switch>
          <Route path="/">
            <ServiceProvider>
              <Services />
            </ServiceProvider>
          </Route>
        </Switch>
      </main>
    </div>
  );
};
